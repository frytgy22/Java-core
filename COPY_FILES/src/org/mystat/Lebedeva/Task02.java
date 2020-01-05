package org.mystat.Lebedeva;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
Задание №2

Создать программу для поиска файлов и папок.
На жестком диске или SSD содержится огромное количество файлов и папок.
При необходимости поиска файла по названию или расширению это занимает достаточно много времени.
Для ускорения поиска целесообразно создать индекс всех файлов в файловой системе.
Для этого необходимо рекурсивно обойти всех файлы и папки на всех дисках (для получения массива с дисками
используйте метод File.listRoots()) и сохранить информацию в отдельном текстовом или бинарном файле.
В дальнейшем поиск будет выполняться не по файловой системе, а по сохраненному индексному файлу.

Приложение должно иметь меню:

1. Создать индекс для всех дисков
2. Искать файл (по регулярному выражению)
	2.1 На всех дисках
	2.2 На конкретном диске
	2.3 В конкретной папке
3. Искать папку (по регулярному выражению)
	3.1 На всех дисках
	3.2 На конкретном диске
	3.3 В конкретной папке
4. Выход

 */
public class Task02 {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Scanner scanner = new Scanner(System.in);
        List<String> listDriversName = new ArrayList<>();
        File file = new File("driversName.txt");
        if (file.exists() && file.length() > 0) {
            try (InputStream inputStream = new FileInputStream(file)) {
                ObjectInputStream inputStream1 = new ObjectInputStream(inputStream);
                listDriversName = (List<String>) inputStream1.readObject();
            }
        }
        boolean exit = false;
        do {
            System.out.println("Menu:\n1 - Create index for all drives.");
            System.out.println("2 - Search for a file.");
            System.out.println("3 - Search for a folder.");
            System.out.println("4 - exit.");
            if (scanner.hasNext()) {
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1: {
                        getDrivers(listDriversName);
                        try (OutputStream outputStream = new FileOutputStream("driversName.txt")) {
                            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                            objectOutputStream.writeObject(listDriversName);
                        }

//                        Files.walk(Paths.get("D:\\"))
//                                .filter(Objects::nonNull).filter(Files::isReadable)
//                                .forEach(System.out::println);//AccessDeniedException
                        break;
                    }
                    case 2: {
                        if (listDriversName.size() > 0) {
                            System.out.println("1 - On all drives.\n2 - On a specific drive.\n3 - In a specific folder.");
                            if (scanner.hasNext()) {
                                Scanner scanner1 = new Scanner(System.in);
                                int choiceFile = scanner.nextInt();
                                System.out.println("Enter file name:");
                                String fileName = scanner1.nextLine();
                                switch (choiceFile) {
                                    case 1: {
                                        List<String> list = searchOnAllDrivers(fileName, "|", listDriversName);
                                        break;
                                    }
                                    case 2: {
                                        List<String> list = searchOnDriver(fileName, "|", getDriverName());
                                        list.forEach(System.out::println);
                                        break;
                                    }
                                    case 3: {
                                        System.out.println("Enter directory: ");
                                        String directoryNameForSearch = scanner1.nextLine();
                                        List<String> list = searchOnAllDrivers("\\\\" + directoryNameForSearch + "\\\\" + fileName, "|", listDriversName);
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
                        } else {
                            System.out.println("Go to case 1!");
                        }
                        break;
                    }
                    case 3: {
                        if (listDriversName.size() > 0) {
                            System.out.println("1 - On all drives.\n2 - On a specific drive.\n3 - In a specific folder.");
                            if (scanner.hasNext()) {
                                int choiceDirectory = scanner.nextInt();
                                System.out.println("Enter directory name:");//какую папку искать
                                Scanner scanner1 = new Scanner(System.in);
                                String directoryName = scanner1.nextLine();
                                switch (choiceDirectory) {
                                    case 1: {
                                        List<String> list = searchOnAllDrivers(directoryName, ">", listDriversName);
                                        break;
                                    }
                                    case 2: {
                                        List<String> list = searchOnDriver(directoryName, ">", getDriverName());
                                        list.forEach(System.out::println);
                                        break;
                                    }
                                    case 3: {
                                        System.out.println("In which folder to search? ");//в какой папке искать
                                        String directoryNameForSearch = scanner1.nextLine();
                                        List<String> list = searchOnAllDrivers("\\\\" + directoryNameForSearch + "\\\\" + directoryName, ">", listDriversName);
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
                        } else {
                            System.out.println("Go to case 1.");
                        }
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
     * метод для получения всех дисков на ПК и записи в список по именам
     *
     * @return лист с именами
     */
    public static List<String> getDrivers(List<String> listDriversName) throws IOException {
        File[] paths = File.listRoots();
        for (File file : paths) {
            listDriversName.add(file.getAbsolutePath());
            try (OutputStream outputStream = new FileOutputStream(file.getAbsolutePath().replaceAll("[:\\\\/*><|]", "") + ".txt")) {
                List<String> driversList = new ArrayList<>();
                String s = getDirectory(new File(file.getAbsolutePath()), driversList);
                outputStream.write(s.getBytes());
            }
        }
        return listDriversName;
    }

    /**
     * метод сканирует файл и сохраняет в отсортированном по названию виде все файлы в строку
     * Директории в конце помечаются >, а файлы |
     *
     * @param file диск для сканирования
     * @param list куда будут записываться все файлы с диска
     * @return строка со всеми файлами диска
     */
    public static String getDirectory(File file, List<String> list) {
        try {
            if (file.exists() && file.canRead()) {
                if (file.isDirectory() && file.canRead()) {
                    list.add(file.getAbsolutePath() + ">\n");
                    File[] files = file.listFiles();
                    if (files != null) {
                        for (File value : files) {
                            getDirectory(value, list);
                        }
                    }
                } else {
                    list.add(file.getAbsolutePath() + "|\n");
                }
            }
        } catch (NullPointerException e) {
            System.err.println("Access error");
        }
        Collections.sort(list);
        return list.toString();
    }

    /**
     * метод запрашивает имя диска
     *
     * @return строку с именем диска
     */
    public static String getDriverName() {
        Scanner scanner1 = new Scanner(System.in);
        String driverName = "";
        do {
            System.out.println("Enter disk name:");
            driverName = scanner1.nextLine().trim();
        } while (!driverName.matches("^[a-zA-Zа-яА-Я]+"));
        return driverName;
    }

    /**
     * метод для поиска файла на конкретном диске
     *
     * @param driverName имя диска
     * @param filename   имя искомого файла
     * @param typeFiles  для фильтрации поска | - файл, > - папка
     * @return список с найденными совпадениями
     * @throws IOException
     */
    public static List<String> searchOnDriver(String filename, String typeFiles, String driverName) throws IOException {
        filename = "(\\.*.*|^)" + filename + "(\\W*.*)";
        String finalFilename = filename;
        if (!new File(driverName + ".txt").exists()) {
            System.out.println("The driver not found.");
            return new ArrayList<>();
        }
        return Files.lines(Paths.get(driverName + ".txt"))
                .filter(s -> s.contains(typeFiles))//пометка, что это файл или папка
                .filter(s -> s.matches(finalFilename))//поиск самого файла
                .flatMap(s -> Stream.of(s.substring(2, s.length() - 1)))
                .collect(Collectors.toList());
    }

    /**
     * метод для поиска файлов на всех дисках
     *
     * @param filename    имя искомого файла
     * @param typeFiles   для фильтрации поска | - файл, > - папка
     * @param driversName список с названиями всех дисков на ПК
     * @return список с найденными совпадениями
     * @throws IOException
     */
    public static List<String> searchOnAllDrivers(String filename, String typeFiles, List<String> driversName) throws IOException {
        List<String> stringList = new ArrayList<>();
        for (String list : driversName) {
            stringList.addAll(searchOnDriver(filename, typeFiles, list.replaceAll("[:\\\\]+", "")));
        }
        stringList.forEach(System.out::println);
        return stringList;
    }
}
