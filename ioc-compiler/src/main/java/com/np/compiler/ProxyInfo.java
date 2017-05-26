package com.np.compiler;


import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

public class ProxyInfo {

    // 包名
    private String packageName;
    // 代理类名
    private String proxyClassName;
    private TypeElement mTypeElement;

    private static final String PROXY = "$$ViewInject";

    /** 用于存放被注解数据 key: view 的 id, value: view */
    Map<Integer, VariableElement> variableElements = new HashMap<>();

    public ProxyInfo(Elements elementUtils, TypeElement typeElement) {
        this.mTypeElement = typeElement;
        PackageElement packageElement = elementUtils.getPackageOf(typeElement);
        // 获取包的完全限定名称 如: com.np.ioc
        String packageName = packageElement.getQualifiedName().toString();
        String className = getClassName(packageName, typeElement);
        // 组成成代理类名 如: MainActivity$$ViewInject
        this.proxyClassName = className + PROXY;
        this.packageName = packageName;
    }

    /**
     * 假如：包名为 com.np.ioc, Human 在这个包内.
     * People 是 Human 的内部类，typeElement.getQualifiedName(): com.np.ioc.Human.People
     * 而不是 com.np.ioc.Human$People
     * @param packageName 包名
     * @param typeElement 代表类的 TypeElement
     * @return 类名
     */
    private String getClassName(String packageName, TypeElement typeElement) {
        // 如包名为: com.np.ioc
        int packageLength = packageName.length() + 1;
        // 类名为: com.np.ioc.MainActivity$A (A 是 MainActivity 的内部类)
        // typeElement.getQualifiedName() 返回的是 com.np.ioc.MainActivity.A
        // 需要返回的名称为: MainActivity$A
        return typeElement.getQualifiedName().toString().substring(packageLength)
                .replace(".", "$");
    }

    // 生成 java 源代码
    public String generateJavaCode() {
        StringBuilder builder = new StringBuilder();
        builder.append("package " + packageName + ";").append("\n")
                .append("import com.np.ioc.*;").append("\n")
                .append("public class " + proxyClassName + " implements "
                        + "ViewInject<" + mTypeElement.getQualifiedName() + "> {").append("\n");
        generateMethods(builder);
        builder.append("}").append("\n");
        return builder.toString();
    }

    private void generateMethods(StringBuilder builder) {
        String fourSpace = "    "; // 4 个空格
        builder.append(fourSpace).append("@Override").append("\n");
        builder.append(fourSpace).append("public void inject("
                + mTypeElement.getQualifiedName() + " host, Object source) {")
                .append("\n");
        for (Integer viewId : variableElements.keySet()) {
            VariableElement variableElement = variableElements.get(viewId);
            String name = variableElement.getSimpleName().toString(); // 名字
            String type = variableElement.asType().toString(); // 类型
            builder.append(fourSpace).append(fourSpace)
                    .append("if (source instanceof android.app.Activity) {").append("\n");
            builder.append(fourSpace).append(fourSpace).append(fourSpace)
                    .append("host." + name + " = (" + type + ") ")
                    .append("(((android.app.Activity) source).findViewById(" + viewId + "));")
                    .append("\n");
            builder.append(fourSpace).append(fourSpace)
                    .append("} else {").append("\n");
            builder.append(fourSpace).append(fourSpace).append(fourSpace)
                    .append("host." + name + " = (" + type + ")")
                    .append("(((android.view.View) source).findViewById(" + viewId + "));")
                    .append("\n");
            builder.append(fourSpace).append(fourSpace)
                    .append("}").append("\n");
        }
        builder.append(fourSpace).append("}").append("\n");
    }

    // Generated code. Do not modify!
//    package com.zhy.viewinject_sample;
//    import com.zhy.ioc.*;
//    public class CategoryActivity$$ViewInject implements ViewInject<com.zhy.viewinject_sample.CategoryActivity> {
//        @Override
//        public void inject(com.zhy.viewinject_sample.CategoryActivity host, Object source) {
//            if (source instanceof android.app.Activity) {
//                host.mCategoryLv = (android.widget.ListView) (((android.app.Activity) source).findViewById(2131492944));
//
//            } else {
//                host.mCategoryLv = (android.widget.ListView) (((android.view.View) source).findViewById(2131492944));
//
//            }
//            ;
//        }
//
//    }

    public String getProxyFullClassName() {
        return packageName + "." + proxyClassName;
    }

    public TypeElement getTypeElement() {
        return mTypeElement;
    }

    public static void main(String[] args) {
        String packageName = "com.np.compiler";
        int packageLength = packageName.length() + 1;
        String className = A.class.getName();
        String fullClassName = className.substring(packageLength);
    }

    class A {

    }
}
