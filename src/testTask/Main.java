package testTask;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Main {
    //путь к папке где находятся файлы для считывания и записи
    public static final String PATH = "C:\\Users\\Максим\\Desktop\\Test\\";
    //режим проверки файлов
    public static final boolean HARD_MODE_OF_CHECK = true;

    public static void main(String[] args) throws Exception {
        int indexStart = 0;//индвкс с которого начинается перечисление входных файлов
        int indexEnd = args.length - 1; //индвкс которым заканчивается перечисление входных файлов
        String typeOfSort = null;//тип сортировки -a или -d (необязательный параметр, по умолчанию -a)
        String typeOfFile = null;//тип данных -i или -s
        String fileOut = null;//имя выходного файла

        //по условию задачи для правильной работы программы должно быть не менее трех введнных параметров: вид файла, выходной файл, входной файл
        if (args.length < 3) {
            throw new Exception("Недостаточно входных данных. Введите параметры в соответствии с ТЗ");
        }

        //проверим минимальные условия работы программы, когда есть вид файла, выходной файл и входной файл (по умолчанию сортировка по возрастанию)
        if (args.length == 3) {
            typeOfSort = "-a";
            fileOut = args[1];
            indexStart = 2;
            if (args[0].equals("-i")) {
                typeOfFile = "-i";
            } else if (args[0].equals("-s")) {
                typeOfFile = "-s";
            } else {
                throw new Exception("Неверно указан вид входных файлов. Должен быть -i или -s");
            }
        }

        //работа программы при 4 и более введенных параметров
        if (args.length > 3) {
            //проверяем ввели ли мы первым первым аргументом тип сортировки
            if (args[0].equals("-a") || args[0].equals("-d")) {
                fileOut = args[2];
                indexStart = 3;
                if (args[0].equals("-a")) {
                    typeOfSort = "-a";
                } else {
                    typeOfSort = "-d";
                }

                if (args[1].equals("-i")) {
                    typeOfFile = "-i";
                } else if (args[1].equals("-s")) {
                    typeOfFile = "-s";
                } else {
                    throw new Exception("Неверно указан второй параметр - вид входных файлов (-i или -s)");
                }
            }
            //если нет, то на первом месте будет параметр указывающий на вид файла, а тип сортировки по умолчанию -a
            else if (args[0].equals("-i") || args[0].equals("-s")) {
                typeOfSort = "-a";
                fileOut = args[1];
                indexStart = 2;
                if (args[0].equals("-i")) {
                    typeOfFile = "-i";
                } else {
                    typeOfFile = "-s";
                }
            } else {
                throw new Exception("Неверно указаны параметры. Введите данные согласно ТЗ");
            }
        }

        //проверим существование выходного файла. Если его нет, то создадим его с таким же именем как указано в параметрах
        if (!Files.exists(Paths.get(PATH + fileOut))) {
            System.out.println("Выходного файла " + (PATH + fileOut) + " не существует. Создадим пустой файл с таким же именем.");
            Files.createFile(Paths.get(PATH + fileOut));
        }

        //проверим существование входных файлов. Если хотя бы одного нет, то предупредим об этом
        List<String> listNoExistFiles = new ArrayList<>();
        for (int i = indexStart; i <= indexEnd; i++) {
            if (!Files.exists(Paths.get(PATH + args[i]))) {
                listNoExistFiles.add(PATH + args[i]);
            }
        }

        if (!listNoExistFiles.isEmpty()) {
            for (String filePath : listNoExistFiles) {
                System.out.println("Входного файла " + filePath + " не существует.");
            }
            throw new Exception("Входных файлов не существует.");
        }


        //будем загонять наши файлы в стэк для их дальнейшей обработки
        Stack<String> stack = new Stack<>();
        for (int i = indexStart; i <= indexEnd; i++) {
            //проверяем входящие файлы на правильность сортировки и наличие неправильных символов
            //создаем временные проверочные файлы с которыми будем в дальнейшем работать
            String tempFileName = checkFile(args[i], typeOfSort, typeOfFile);
            stack.push(tempFileName);
        }
        //пока в стэке не останется единственный смердженный файл
        while (stack.size() != 1) {
            //соединяем два верхних файла из стэка
            String pathMergeFile = mergeSortFiles(stack.pop(), stack.pop(), typeOfSort, typeOfFile);
            stack.push(pathMergeFile);//добавляем смердженный файл наверх стэка и следующей итерацией его же и соединяем
        }
        //берем оставшийся смердженный файл из стэка и записываем в выходной файл
        writeFromFilesToOutFile(stack.pop(), fileOut);

    }

    //вспомогательные методы
    //метод проверки данных файла, а именно его принадлежность к одному из двух типов данных (-i или -s) и является ли он отсортированным
    public static String checkFile(String fileName, String typeOfSort, String typeOfFile) throws IOException {
        String tempFile = "check" + fileName;
        String pathTempFile = PATH + tempFile;
        Files.createFile(Paths.get(pathTempFile));//временный файл куда поместим проверенные данные
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(PATH + fileName)));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathTempFile)));
        String line;

        if (typeOfFile.equals("-i")) {
            int number;
            Integer previousNumber = null;//переменная для сравнения с предыдущим значением
            while ((line = reader.readLine()) != null) {
                //проверяем что строка имеет числовой формат без пробелов, если нет пропускаем данную строку и не записываем во временный файл
                try {
                    number = Integer.parseInt(line);
                } catch (NumberFormatException e) {
                    continue;
                }
                //записываем первое число во временный файл
                if (previousNumber == null) {
                    previousNumber = number;
                    writer.write(line);
                    writer.newLine();
                    continue;
                }

                if (typeOfSort.equals("-a")) {
                    //проверяем, что последующий записываемый элемент во временный файл больше либо равен предыдущему,
                    //если нет то в зависимости от выбранного нами режима проверки (HARD_MODE_OF_CHECK) либо отбрасываем данные в файле,
                    //либо пытаемся найти следующий подходящий элемент
                    if (number >= previousNumber) {
                        previousNumber = number;
                        writer.write(line);
                        writer.newLine();
                    } else {
                        if (HARD_MODE_OF_CHECK) {
                            break;
                        }
                    }
                }

                if (typeOfSort.equals("-d")) {
                    //проверяем, что последующий записываемый элемент во временный файл меньше либо равен предыдущему,
                    if (number <= previousNumber) {
                        previousNumber = number;
                        writer.write(line);
                        writer.newLine();
                    } else {
                        if (HARD_MODE_OF_CHECK) {
                            break;
                        }
                    }
                }
            }
        }

        if (typeOfFile.equals("-s")) {
            String previousLine = null;
            while ((line = reader.readLine()) != null) {
                //проверяем содержит ли строка пробел (или вообще пустая), если да то переходим к следующей итерации
                if (line.contains(" ") || line.isEmpty()) {
                    continue;
                }

                if (previousLine == null) {
                    previousLine = line;
                    writer.write(line);
                    writer.newLine();
                    continue;
                }

                if (typeOfSort.equals("-a")) {
                    //проверяем, что строки отсортированы по возрастанию
                    if (line.compareTo(previousLine) >= 0) {
                        previousLine = line;
                        writer.write(line);
                        writer.newLine();
                    } else {
                        if (HARD_MODE_OF_CHECK) {
                            break;
                        }
                    }
                }

                if (typeOfSort.equals("-d")) {
                    //проверяем, что строки отсортированы по убыванию
                    if (line.compareTo(previousLine) <= 0) {
                        previousLine = line;
                        writer.write(line);
                        writer.newLine();
                    } else {
                        if (HARD_MODE_OF_CHECK) {
                            break;
                        }
                    }
                }
            }
        }
        reader.close();
        writer.close();

        return tempFile;//имя созданного временного файла
    }

    public static String mergeSortFiles(String fileName1, String fileName2, String typeOfSort, String typeOfFile) throws IOException {
        String tempFile = "merge" + fileName1;
        String pathTempFile = PATH + tempFile;
        Files.createFile(Paths.get(pathTempFile));//временный файл куда поместим данные смердженных файлов
        BufferedReader reader1 = new BufferedReader(new InputStreamReader(new FileInputStream(PATH + fileName1)));
        BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(PATH + fileName2)));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathTempFile)));
        String line1;
        String line2;

        //алгоритм слияния для числовых данных в файле
        if (typeOfFile.equals("-i")) {
            while ((line1 = reader1.readLine()) != null | (line2 = reader2.readLine()) != null) {
                while (true) {
                    //если данные в одном из файлов кончились, то записываем оставшиеся данные из другого файла в конец
                    if (line1 == null) {
                        while (line2 != null) {
                            writer.write(line2);
                            writer.newLine();
                            line2 = reader2.readLine();
                        }
                        break;
                    } else if (line2 == null) {
                        while (line1 != null) {
                            writer.write(line1);
                            writer.newLine();
                            line1 = reader1.readLine();
                        }
                        break;
                        //во всех других случаях
                    } else {
                        if (Integer.parseInt(line1) == Integer.parseInt(line2)) {
                            writer.write(line1);
                            writer.newLine();
                            writer.write(line2);
                            writer.newLine();
                            break;//если два элемента равны нас кидает во внешний цикл для итерации в обоих файлах
                        }

                        if (typeOfSort.equals("-a")) {
                            if (Integer.parseInt(line1) < Integer.parseInt(line2)) {
                                writer.write(line1);
                                writer.newLine();
                                line1 = reader1.readLine();//считываем меньшее число и делаем итерацию в данном файле
                            } else {
                                writer.write(line2);
                                writer.newLine();
                                line2 = reader2.readLine();//проделываем итерацию в другом файле
                            }
                        }

                        if (typeOfSort.equals("-d")) {
                            if (Integer.parseInt(line1) > Integer.parseInt(line2)) {
                                writer.write(line1);
                                writer.newLine();
                                line1 = reader1.readLine();//считываем большее число и делаем итерацию в данном файле
                            } else {
                                writer.write(line2);
                                writer.newLine();
                                line2 = reader2.readLine();//проделываем итерацию в другом файле;
                            }
                        }
                    }
                }
            }
        }

        //алгоритм слияния для строковых данных в файле
        if (typeOfFile.equals("-s")) {
            while ((line1 = reader1.readLine()) != null | (line2 = reader2.readLine()) != null) {
                while (true) {
                    //если данные в одном из файлов кончились, то записываем оставшиеся данные из другого файла в конец
                    if (line1 == null) {
                        while (line2 != null) {
                            writer.write(line2);
                            writer.newLine();
                            line2 = reader2.readLine();
                        }
                        break;
                    } else if (line2 == null) {
                        while (line1 != null) {
                            writer.write(line1);
                            writer.newLine();
                            line1 = reader1.readLine();
                        }
                        break;
                        //во всех других случаях
                    } else {
                        if (line1.compareTo(line2) == 0) {
                            writer.write(line1);
                            writer.newLine();
                            writer.write(line2);
                            writer.newLine();
                            break;//если два элемента равны нас кидает во внешний цикл для итерации в обоих файлах
                        }

                        if (typeOfSort.equals("-a")) {
                            if (line1.compareTo(line2) < 0) {
                                writer.write(line1);
                                writer.newLine();
                                line1 = reader1.readLine();//считываем меньшую строку и делаем итерацию в данном файле
                            } else {
                                writer.write(line2);
                                writer.newLine();
                                line2 = reader2.readLine();//проделываем итерацию в другом файле
                            }
                        }

                        if (typeOfSort.equals("-d")) {
                            if (line1.compareTo(line2) > 0) {
                                writer.write(line1);
                                writer.newLine();
                                line1 = reader1.readLine();//считываем большую строку и делаем итерацию в данном файле
                            } else {
                                writer.write(line2);
                                writer.newLine();
                                line2 = reader2.readLine();//проделываем итерацию в другом файле
                            }
                        }
                    }
                }
            }
        }
        reader1.close();
        reader2.close();
        writer.close();

        //удаляем ненужные временные файл
        Files.delete(Paths.get(PATH + fileName1));
        Files.delete(Paths.get(PATH + fileName2));

        return tempFile;
    }

    //метод для записи содержимиого итогового временного файла в выходной файл
    public static void writeFromFilesToOutFile(String fileIn, String fileOut) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(PATH + fileIn)));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(PATH + fileOut)));
        String line;
        while ((line = reader.readLine()) != null) {
            writer.write(line);
            writer.newLine();
        }
        reader.close();
        writer.close();
        Files.delete(Paths.get(PATH + fileIn));//удаляем наш временный файл в папке
    }
}