package com.np.compiler;

import com.google.auto.service.AutoService;
import com.np.annotation.BindView;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * 注解处理器
 * AutoService 注解的作用：就不用再手动配置 META-INF 文件了.
 */
@AutoService(Processor.class)
public class IocProcessor extends AbstractProcessor {

    /** 跟文件相关的辅助类,生成 JavaSourceCode. */
    private Filer mFiler;
    /** 根元素相关的辅助类,帮助我们去获取一些元素相关的信息. */
    private Elements mElementUtils;
    /** 跟日志相关的辅助类. */
    private Messager mMessager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        System.out.println("init.......................");
        mFiler = processingEnv.getFiler();
        mElementUtils = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
    }

    // 返回支持的注解类型
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        System.out.println("getSupportedAnnotationTypes.......................");
        Set<String> annotationTypes = new LinkedHashSet<>();
        annotationTypes.add(BindView.class.getCanonicalName());
        return annotationTypes;
    }

    // 返回支持的源码版本
    @Override
    public SourceVersion getSupportedSourceVersion() {
        System.out.println("getSupportedSourceVersion.......................");
        return SourceVersion.latestSupported();
    }

    /**
     * 存放 代表具体某个类的代理类生成的全部信息,key 为类的全类名.
     */
    private Map<String, ProxyInfo> mProxyInfoMap = new HashMap<>();

    /**
     * 两个步骤:
     * 1、收集信息: 根据你的注解声明, 拿到对应的 Element, 然后获取我们所需要的信息,
     *      这个信息肯定是为了后面生成的 JavaFileObject 所准备的.
     * 2、生成代理类(把编译生成的类叫代理类)
     *
     * Element
         - VariableElement //一般代表成员变量
         - ExecutableElement //一般代表类中的方法
         - TypeElement //一般代表类
         - PackageElement //一般代表Package
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("process.......................");
        mProxyInfoMap.clear();
        // 1、收集信息
        // 获取类中通过 BindView 注解的成员变量集合.
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BindView.class);
        for (Element element : elements) {
            if (!checkAnnotationUseValid(element, BindView.class)) {
                return false;
            }
            // 强转为 VariableElement（类的成员变量）
            VariableElement variableElement = (VariableElement) element;
            // 通过 VariableElement 获取 TypeElement （存在注解成员变量的类）
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();
            // 获取类的全类名
            String qualifiedName = typeElement.getQualifiedName().toString();

            ProxyInfo proxyInfo = mProxyInfoMap.get(qualifiedName);
            if (proxyInfo == null) {
                proxyInfo = new ProxyInfo(mElementUtils, typeElement);
                mProxyInfoMap.put(qualifiedName, proxyInfo);
            }
            BindView annotation = variableElement.getAnnotation(BindView.class);
            int viewId = annotation.value();
            proxyInfo.variableElements.put(viewId, variableElement);
        }

        // 2、生成代理类代码
        for (String key : mProxyInfoMap.keySet()) {
            ProxyInfo proxyInfo = mProxyInfoMap.get(key);
            try {
                // 创建 Java 源文件.
                JavaFileObject sourceFile = mFiler.createSourceFile(
                        proxyInfo.getProxyFullClassName(),
                        proxyInfo.getTypeElement());
                Writer writer = sourceFile.openWriter();
                writer.write(proxyInfo.generateJavaCode());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                error(proxyInfo.getTypeElement(),
                        "Unable to write injector for type %s：%s",
                        proxyInfo.getTypeElement(), e.getMessage());
            }
        }
        return true;
    }

    /** 检查 Element 的类型是否为 VariableElement(即成员变量) 和 不为私有的. */
    private boolean checkAnnotationUseValid(Element element, Class clazz) {
        if (element.getKind() != ElementKind.FIELD) {
            error(element, "%s must be declared on field.", clazz.getSimpleName());
            return false;
        } else if (isPrivate(element)) {
            error(element, "%s must not be private.", element.getSimpleName());
            return false;
        }
        return true;
    }

    /** 判断元素的修饰符是否为私有类型 */
    private boolean isPrivate(Element element) {
        return element.getModifiers().contains(Modifier.PRIVATE);
    }

    /** 输出错误日志 */
    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        mMessager.printMessage(Diagnostic.Kind.NOTE, message, element);
    }

    // 测试 getCanonicalName() 和 getName() 的区别.
//    public static void main(String[] args) {
//        // com.np.compiler.IocProcessor
//        String canonicalName1 = IocProcessor.class.getCanonicalName();
//        // com.np.compiler.IocProcessor
//        String name2 = IocProcessor.class.getName();
//
//        // com.np.compiler.IocProcessor.A
//        String canonicalName = A.class.getCanonicalName();
//        // com.np.compiler.IocProcessor$A
//        String name = A.class.getName();
//
//    }
//
//     class A {}
}
