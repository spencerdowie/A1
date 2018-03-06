package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.awt.*;
import java.io.*;
import java.util.*;
// **************************************************//

public class SpamTrainer {
    private Map<String, Integer> spamCounts, hamCounts;
    private Map<String, Integer> mapReference;
    private Double spamFiles = 0.0, hamFiles = 0.0, numTruePositives = 0.0, numTrueNegatives = 0.0, numFalsePositives = 0.0;
    public Double accuracy = 0.0, precision = 0.0;

    public enum DataType
    {
        Ham,
        Spam
    }

    // Constructor creating necessary maps
    public SpamTrainer()
    {
        spamCounts = new TreeMap<>();
        hamCounts = new TreeMap<>();
    }

    // Check if word is real
    private boolean isWord(String word)
    {
        String pattern = "^[a-z]+$";
        return word.matches(pattern);
    }

    // Add word to TreeMap
    private void addWordToMap(String word, DataType type)
    {
        // Set Correct Reference
        if(type == DataType.Ham)
            mapReference = hamCounts;
        else
            mapReference = spamCounts;

        // Add word to relevent map
        if(mapReference.containsKey(word))
        {
            int oldCount = mapReference.get(word);
            mapReference.put(word, oldCount+1);
        }
        else
            mapReference.put(word, 1);
    }

    public ObservableList<TestFile> processTestFolder(File file) throws IOException
    {

        ObservableList<TestFile> files = FXCollections.observableArrayList();
        File[] folders = file.listFiles();

        for (File folder: folders) {
            if (folder.isDirectory()) {
                String fileName = folder.getName();
                String actualClass;

                if (fileName.contains("ham")) {
                    actualClass = "ham";
                } else {
                    actualClass = "spam";
                }
                File[] contents = folder.listFiles();
                for (File current : contents) {

                    if(current.getName() == "cmds")
                        break;

                    TestFile test = new TestFile(current.getName(), testFile(current), actualClass);
                    files.add(test);
                    if(test.getSpamProbability() > 0.5) {
                        if (actualClass.matches("ham")) {
                            numFalsePositives++;
                        }
                        else
                        {
                            numTruePositives++;
                        }
                    }
                    else
                    {
                        if (actualClass.matches("ham")) {
                            numTrueNegatives++;
                        }
                    }
                }
            }
        }
        accuracy = (numTruePositives + numTrueNegatives) / files.size();
        precision = numTruePositives / (numFalsePositives + numTruePositives);
        return files;
    }

    public void processTrainFolder(File file) throws IOException
    {
        File[] folders = file.listFiles();

        for (File folder: folders) {
            if (folder.isDirectory()) {
                String fileName = folder.getName();
                if (fileName.contains("ham")) {

                    File[] contents = folder.listFiles();
                    for (File current : contents) {
                        hamFiles++;
                        processData(current, DataType.Ham);
                    }
                } else if (fileName.contains("spam")) {

                    File[] contents = folder.listFiles();
                    for (File current : contents) {
                        spamFiles++;
                        processData(current, DataType.Spam);
                    }
                }
            }
        }
    }

    public void processData(File file, DataType data) throws IOException
    {
        if(file.exists())
        {
            Scanner scanner = new Scanner(file);
            scanner.useDelimiter("[\\s\\.;:\\?\\!,]");
            while(scanner.hasNext())
            {
                String word = scanner.next();
                word = word.toLowerCase();
                if(isWord(word))
                {
                    addWordToMap(word, data);
                }
            }
        }
    }


    // Returns spam probability of file (training is assumed to be complete)
    public Double testFile(File file) throws IOException
    {
        Double n = 0.0;

        if(file.exists())
        {
            Scanner scanner = new Scanner(file);
            scanner.useDelimiter("[\\s\\.;:\\?\\!,]");
            while(scanner.hasNext())
            {
                String word = scanner.next();
                word = word.toLowerCase();
                if(isWord(word)) {
                    if (spamCounts.containsKey(word) && hamCounts.containsKey(word)) {
                        Double PSW, PWS, PWH;

                        PWS = spamCounts.get(word) / spamFiles;
                        PWH = hamCounts.get(word) / hamFiles;
                        PSW = PWS / (PWS + PWH);

                        n += Math.log(1 - PSW) - Math.log(PSW);
                    }
                }
            }
        }

        Double PSF = 1/(1+Math.pow(Math.E, n));
        return PSF;
    }
}
