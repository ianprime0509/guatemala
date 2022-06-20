package dev.ianjohnson.guatemala.examples.gtk;

import io.github.classgraph.AnnotationParameterValueList;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public final class Main {
    public static void main(String[] args) {
        List<RunnableExample> examples = findExamples();
        for (int i = 0; i < examples.size(); i++) {
            RunnableExample example = examples.get(i);
            System.out.println((i + 1) + ". " + example.name() + " - " + example.description());
        }
        inputExample(examples).ifPresent(example -> {
            Class<?> exampleClass = Class.forName(Main.class.getModule(), example.className());
            assert exampleClass != null;
            try {
                exampleClass.getDeclaredMethod("main", String[].class).invoke(null, (Object) args);
            } catch (Exception e) {
                System.err.println("Example threw exception:");
                e.printStackTrace(System.err);
                System.exit(1);
            }
        });
    }

    private static List<RunnableExample> findExamples() {
        try (ScanResult scanResult = new ClassGraph()
                .enableAnnotationInfo()
                .acceptPackages(Main.class.getPackageName())
                .scan()) {
            return scanResult.getClassesWithAnnotation(Example.class).stream()
                    .map(classInfo -> {
                        AnnotationParameterValueList annotationParams =
                                classInfo.getAnnotationInfo(Example.class).getParameterValues();
                        return new RunnableExample(
                                (String) annotationParams.getValue("name"),
                                (String) annotationParams.getValue("description"),
                                classInfo.getName());
                    })
                    .toList();
        }
    }

    private static Optional<RunnableExample> inputExample(List<RunnableExample> examples) {
        System.out.print("Choose an example: ");
        System.out.flush();
        Scanner input = new Scanner(System.in);
        while (input.hasNextLine()) {
            String line = input.nextLine();
            int choice;
            try {
                choice = Integer.parseInt(line) - 1;
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice: " + line);
                System.out.print("Choose an example: ");
                System.out.flush();
                continue;
            }
            if (choice < 0 || choice >= examples.size()) {
                System.out.println("Invalid choice: " + line);
                System.out.print("Choose an example: ");
                System.out.flush();
                continue;
            }
            return Optional.of(examples.get(choice));
        }
        return Optional.empty();
    }

    private record RunnableExample(String name, String description, String className) {}
}
