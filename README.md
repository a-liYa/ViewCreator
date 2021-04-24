**ViewCreator**

Android 布局加载性能优化 - 反射改成new

```
android-28
HashMap<String, Constructor<?>> sConstructorMap = new HashMap<>()

LayoutInflater#sConstructorMap;
GenericInflater#sConstructorMap;
ReflectionInflater#sConstructorMap;

androidx
androidx.appcompat.app.AppCompatViewInflater#sConstructorMap;
```

#### 原理

通过 LayoutInflater.setFactory2 拦截 View 的反射创建改成new

#### 分析过程

加载 layout 常用方法

1. View#inflate(Context, int, ViewGroup)
2. Activity#setContentView(int)
3. LayoutInflater#inflate(int, ViewGroup, boolean)

最终全部调用的方法3 LayoutInflater.inflate() 

追踪 LayoutInflater 源码

LayoutInflater.java // abstract class

```java
View inflate(int, ViewGroup, boolean)
  -> View inflate(XmlPullParser, ViewGroup, boolean)
    -> View createViewFromTag(View, String, Context, AttributeSet)
	  -> View createViewFromTag(View, String, Context, AttributeSet, boolean) 
```

```java
View createViewFromTag(View parent, String name, Context context, AttributeSet attrs,
        boolean ignoreThemeAttr) {
	...
	if (mFactory2 != null) {
		view = mFactory2.onCreateView(parent, name, context, attrs);
	} else if (mFactory != null) {
		view = mFactory.onCreateView(name, context, attrs);
	}
	...
	if (view == null) {
		if (-1 == name.indexOf('.')) {
			view = onCreateView(parent, name, attrs);
		} else { // 反射代码
			view = createView(name, null, attrs);
		}
	}
}
```

```java
View onCreateView(View, String, AttributeSet)
  -> View onCreateView(String, AttributeSet)
    -> View createView(String, "android.view.", AttributeSet) // 反射代码
```

PhoneLayoutInflater.java

```java
@Override
protected View onCreateView(String name, AttributeSet attrs) {
	// sClassPrefixList = {"android.widget.", "android.webkit.", "android.app."}
    for (String prefix : sClassPrefixList) {
        try { // 反射代码
            View view = createView(name, prefix, attrs);
            if (view != null) {
                return view;
            }
        } catch (ClassNotFoundException e) {
            // In this case we want to let the base class take a crack at it.
        }
    }
    return super.onCreateView(name, attrs); // createView(name, "android.view.", attrs)
}
```

LayoutInflater.Factory2 实现类

```java
class LayoutInflaterFactory implements LayoutInflater.Factory2 {
	ViewCreatorImpl mViewCreator = new ViewCreatorImpl();
	...
	@Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
    	return mViewCreator.createView(name, context, attrs);
    }
}
```

ViewCreatorImpl.java 

```java
public class ViewCreatorImpl {
   public View createView(String name, Context context, AttributeSet attrs) {
   		switch(name) {
   			case "View" :
				return new View(context,attrs);
   			case "TextView" :
				return new TextView(context,attrs);
			case "androidx.recyclerview.widget.RecyclerView" :
				return new androidx.recyclerview.widget.RecyclerView(context,attrs);
			...
   		}
   		return null;
   }
}
```

ViewCreatorImpl 内部的代码如何生成？

1. 如何知道布局中使用了哪些 view ？
2. switch语句内的代码如何生成？

寻找布局使用的 view

​	根据 android Apk 打包过程可以知道，是一系列 task 的执行，其中有个 task 是 mergeResources，针对 Debug、Release 分别对应 “mergeDebugResources”，“mergeReleaseResources”，通过其 inputFiles，可以获取到所有 module 的 src\main\res\layout 路径 layoutPath，遍历 layoutPath 目录下的 xml文件，使用 XmlSlurper 解析 xml view 标签（对应 view name），并保存到指定文件 view_names.txt。

ViewCreatorImpl 使用 AOP 生成，读取 view_names.txt 文件存储的 view names。

**速度提升**

| samsung s10（android 11）创建 Framelayout | 反射  | new   |
| :---------------------------------------: | ----- | ----- |
|                  10000次                  | 347ms | 316ms |
|                  20000次                  | 662ms | 611ms |
|                  30000次                  | 997ms | 899ms |

*注：实际项目中，创建各种不同的 View ，效率提升应该会大于此单一 View 的结果。*

#### 踩坑

1. mergeDebugResources、mergeReleaseResources 并非唯一，根据自己的项目实际名字来，可通过 gradle -> app -> build -> build 获取；

2. setFactory2() 已被其他代码设置会抛异常 IllegalStateException("A factory has already been set on this LayoutInflater")；

   ​	可参考 FactoryMerger 方式处理。

3. ViewCreatorImpl 内部引用了不能访问的 View class；

   ​	build.gradle 配置 creator_view_ignores.image_view = "ImageView" 即可在存储view_names.txt时忽略 ImageView；

   ​	目前只能通过手动配置，后期寻找自动方案。

4. 依赖的 module A 如果使用 implementation module B，则 module B 内的自定义 View, App 无法访问，依然存在 3 问题。

   ​	解决方案改成 api，此方案不够优雅，待后期优化。

#### 引申

Android 30 LayoutInflater CTS 引入预编译的布局

```java
public abstract class LayoutInflater {
	...
	// Indicates whether we should try to inflate layouts using a precompiled layout instead of
    // inflating from the XML resource.
    private boolean mUseCompiledView;
    
    private void initPrecompiledViews() {
        // Precompiled layouts are not supported in this release.
        boolean enabled = false;
        initPrecompiledViews(enabled);
    }
    
    private void initPrecompiledViews(boolean enablePrecompiledViews) {
        mUseCompiledView = enablePrecompiledViews;
        ...
    }
    
    /**
     * @hide for use by CTS tests
     */
    @TestApi
    public void setPrecompiledLayoutsEnabledForTesting(boolean enablePrecompiledLayouts) {
        initPrecompiledViews(enablePrecompiledLayouts);
    }
    
    View tryInflatePrecompiled(int resource, Resources res, ViewGroup root,
        boolean attachToRoot) {
        if (!mUseCompiledView) {
            return null;
        }
        ...
        // Try to inflate using a precompiled layout.
        String pkg = res.getResourcePackageName(resource);
        String layout = res.getResourceEntryName(resource);

        try {
            Class clazz = Class.forName("" + pkg + ".CompiledView", false, mPrecompiledClassLoader);
            Method inflater = clazz.getMethod(layout, Context.class, int.class);
            View view = (View) inflater.invoke(null, mContext, resource);
			...
            return view;
        }
    }
    
}
```

Android_s_preview1 LayoutInflater.java 代码依然同 Android 30。

