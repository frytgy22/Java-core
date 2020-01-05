package org.mystat.Lebedeva;
/*
Задание №1

Создать программу для рекурсивного копирования директорий.
Под рекурсивным копированием подразумевается копирование всех вложенных файлов и папок.
Приложение должно иметь меню:

1. Указать исходную папку
2. Указать целевую папку
3. Начать копирование
4. Выход

Если при указании исходной папки таковая не будет найдена, то необходимо запросить новую папку.
При отсутсвии целевой папки ее необходимо создать
Если в целевой папке есть содержимое, то его необходимо удалить.
При выходе из программы исходная папка и целевая папки сохраняются в конфигурационном файле,
таким образом при повторном запуске программы и необходимости повторить копирование достаточно
выбрать пункт 3.

Для указания исходной папки можно использовать диалог JFileChooser из пакета java.swing.
Пример создания диалога: https://repl.it/@MaximShaptala/DirectoryChooserDemo
 */


import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Task01 {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        File source = null;
        File target = null;
        OutputStream outputStream = null;
        boolean getOldFiles = true;
        boolean exit = false;
        do {
            System.out.println("Menu:\n1 - Specify source folder.\n2 - Specify destination folder.\n3 - Start copying.\n4 - Exit.");
            if (scanner.hasNext()) {
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1: {
                        do {
                            source = getDirectory();
                        } while (source == null);
                        getOldFiles = false;
                        break;
                    }
                    case 2: {
                        target = getDirectory();
                        break;
                    }
                    case 3: {
                        copyDirectory(source, target, getOldFiles);
                        break;
                    }
                    case 4: {
                        exit = true;
                        break;
                    }
                    default: {
                        System.out.println("Wrong number.");
                        break;
                    }
                }
            } else {
                System.out.println("Error. You entered not number.");
            }
        } while (!exit);
    }

    /**
     * the method in the dialog box takes the path to the folder and transfers to File
     *
     * @return new File
     */
    public static File getDirectory() {
        System.out.println("Specify or create a folder.");//Укажите или создайте папку
        JFileChooser chooser = new JFileChooser(); // Создаем диалог
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // отображать только директории
        int returnVal = chooser.showOpenDialog(null); // показать диалог открытия файла
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            if (chooser.getSelectedFile().isDirectory()) {
                System.out.println("You chose to open this folder: " + chooser.getSelectedFile().getName()); // здесь находится имя выбранной папки
                return chooser.getSelectedFile();
            } else {
                System.out.println("This is not directory.");
            }
        }
        return null;
    }

    /**
     * The method for deleted all files and subdirectories from a directory
     *
     * @param target File to delete its subdirectory
     * @throws IOException
     */
    public static void deleteAllSubdirectory(File target) throws IOException {
        if (target != null) {
            Files.walk(target.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .filter(item -> !item.getPath().equals(target.getPath()))
                    .forEach(File::delete);
        }
    }

    /**
     * The method for copying first directory to second directory
     *
     * @param source from copy
     * @param target to copy
     * @throws IOException
     */
    public static void copyDirectory(File source, File target, boolean getOld) throws IOException {
        if (getOld) {//if you haven’t entered case 1
            String oldFiles = Files.lines(Paths.get("configuration.txt")).collect(Collectors.joining());
            File file = new File(oldFiles.substring(oldFiles.indexOf("[") + 1));
            file.delete();
            source = new File(oldFiles.substring(0, oldFiles.indexOf("[")));
            target = new File(oldFiles.substring(oldFiles.indexOf("[") + 1));
        }
        if (target == null) {
            do {
                target = getDirectory();
            } while (target == null);
        }
        if (target.exists()) {
            deleteAllSubdirectory(target);
            target.delete();
        }

        String wayTarget = target.getAbsolutePath();
        Path sourcePath = source.toPath();
        Path targetPath = Files.createDirectories(Paths.get(wayTarget));

        File finalSource = source;
        try (Stream<Path> walk = Files.walk(sourcePath)) {
            walk.forEach(s -> {
                try {
                    Files.copy(s, targetPath.resolve(sourcePath.relativize(s)), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    String configuration = finalSource.getPath() + "[" + wayTarget;
                    try (OutputStream outputStream1 = new FileOutputStream("configuration.txt")) {
                        outputStream1.write(configuration.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
