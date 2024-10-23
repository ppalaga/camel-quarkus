package io.quarkiverse.cxf.doc.it;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.javadoc.description.JavadocDescription;

public class TransformTest {
    private static final Object parser;
    private static final Method parseMethod;
    static {
        try {
            Class<?> parserClass = Class.forName("io.quarkus.annotation.processor.generate_doc.JavaDocParser");
            Constructor<?> constructor = parserClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            parser = constructor.newInstance();
            parseMethod = parserClass.getDeclaredMethod("parseConfigDescription", String.class);
            parseMethod.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    void toAsciidoclet() throws IOException {
        final Path repoRoot = Paths.get("/home/ppalaga/orgs/cq/camel-quarkus");
        Stream.of("extensions", "extensions-core", "extensions-jvm", "extensions-support")
        .map(repoRoot::resolve)
        .forEach(extensionsDir -> {
            try (Stream<Path> stream = Files.walk(extensionsDir)) {
                stream
                        .filter(Files::isRegularFile)
                        .filter(f -> f.getFileName().toString().endsWith("Config.java"))
                        .map(extensionsDir::resolve)
                        .forEach(this::transformConfig);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void transformConfig(Path file) {
        JavaParser parser = new JavaParser(StaticJavaParser.getParserConfiguration());
        try {
            final CompilationUnit unit = parser.parse(file).getResult().get();

            /* Go through all members recursively, and transform each piece of JavaDoc to AsciiDoc */
            TypeDeclaration<?> primaryType = unit.getType(0);
            if (primaryType.getAnnotationByName("ConfigRoot").isPresent()
                    || primaryType.getAnnotationByName("ConfigGroup").isPresent()) {
                System.out.println("==== " + file);
                walkType(primaryType);
                store(unit, file.getParent());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    static void walkType(TypeDeclaration<?> primaryType) {
        /* methods */
        primaryType.findAll(ClassOrInterfaceDeclaration.class).stream()
        .peek(typeDecl -> System.out.println("== type " + typeDecl.getNameAsString()))
        .flatMap(typeDecl -> typeDecl.findAll(MethodDeclaration.class).stream())
        .forEach(methodDecl -> {
            System.out.println(methodDecl.getNameAsString());

            methodDecl.getJavadoc().ifPresent(javaDoc -> {
                System.out.println("== javadoc " + javaDoc.toText());

                if (javaDoc.getBlockTags().stream().noneMatch(t -> t.getTagName().equals("asciidoclet"))) {
                    try {
                        JavadocDescription description = javaDoc.getDescription();
                        String asciidoc = (String) parseMethod.invoke(parser, description.toText());
                        asciidoc = asciidoc.replace("|", "\\|");
                        asciidoc = asciidoc.replace("link:../../user-guide/configuration.html",
                                "xref:user-guide/configuration.adoc");
                        asciidoc = asciidoc.replace(
                                "\n\n```",
                                "\n\n"
                                        + "[source,properties]\n"
                                        + "----");
                        asciidoc = asciidoc.replace(
                                "```",
                                "----");
                        asciidoc = asciidoc.replace("link:#", "xref:#");
                        asciidoc = asciidoc.replace("link:quarkus-cxf-services-sts.html",
                                "xref:reference/extensions/quarkus-cxf-services-sts.adoc");
                        System.out.println("=== asciidoc " + asciidoc);

                        javaDoc.addBlockTag("asciidoclet");
                        description.getElements().clear();

                        JavadocDescription newDescription = JavadocDescription.parseText(asciidoc);
                        newDescription.getElements().forEach(description::addElement);

                        methodDecl.setJavadocComment(javaDoc);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

        });

        /* fields */
        primaryType.findAll(ClassOrInterfaceDeclaration.class).stream()
        .peek(typeDecl -> System.out.println("== type " + typeDecl.getNameAsString()))
        .flatMap(typeDecl -> typeDecl.findAll(FieldDeclaration.class).stream())
        .forEach(fldDecl -> {
            fldDecl.getJavadoc().ifPresent(javaDoc -> {
                if (fldDecl.getVariables().size() > 1) {
                    throw new IllegalStateException("Cannot handle more than one variable declaration per field :" + fldDecl + " in type " + fldDecl.getParentNode());
                }
                final VariableDeclarator varDecl = fldDecl.getVariables().get(0);
                System.out.println(varDecl.getName());
                System.out.println("== javadoc " + javaDoc.toText());

                if (javaDoc.getBlockTags().stream().noneMatch(t -> t.getTagName().equals("asciidoclet"))) {
                    try {
                        JavadocDescription description = javaDoc.getDescription();
                        String asciidoc = (String) parseMethod.invoke(parser, description.toText());
                        asciidoc = asciidoc.replace("|", "\\|");
                        asciidoc = asciidoc.replace("link:../../user-guide/configuration.html",
                                "xref:user-guide/configuration.adoc");
                        asciidoc = asciidoc.replace(
                                "\n\n```",
                                "\n\n"
                                        + "[source,properties]\n"
                                        + "----");
                        asciidoc = asciidoc.replace(
                                "```",
                                "----");
                        asciidoc = asciidoc.replace("link:#", "xref:#");
                        asciidoc = asciidoc.replace("link:quarkus-cxf-services-sts.html",
                                "xref:reference/extensions/quarkus-cxf-services-sts.adoc");
                        System.out.println("=== asciidoc " + asciidoc);

                        javaDoc.addBlockTag("asciidoclet");
                        description.getElements().clear();

                        JavadocDescription newDescription = JavadocDescription.parseText(asciidoc);
                        newDescription.getElements().forEach(description::addElement);

                        fldDecl.setJavadocComment(javaDoc);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

        });
    }

    private void store(CompilationUnit unit, Path destinationDir) throws IOException {
        String name = unit.getType(0).getNameAsString();
        Path file = destinationDir.resolve(name + ".java");
        final String oldContent = Files.exists(file) ? Files.readString(file) : null;
        final String newContent = unit.toString().replace("@Override()", "@Override");
        if (!newContent.equals(oldContent)) {
            System.out.println("Updating " + name + ".java");
            Files.createDirectories(destinationDir);
            Files.write(file, newContent.getBytes(StandardCharsets.UTF_8));
        } else {
            System.out.println(name + ".java up to date");
        }
    }

}
