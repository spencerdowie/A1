package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
// **************************************************//

public class SpamTrainer {
    private Map<String, Integer> spamCounts, hamCounts;
    private Map<String, Double> ratioCounts;
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
        ratioCounts = new TreeMap<>();
        TotalCommonWords = CommonWords_Array.length;
        CommonWords_List = Arrays.asList(CommonWords_Array);
    }

    // Check if word is real
    private boolean isWord(String word)
    {
        // Ignore Common Words
        //if(CommonWords_List.contains(word))
        //    return false;

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

        // Add word to relevant map
        if(mapReference.containsKey(word))
        {
            int oldCount = mapReference.get(word);
            mapReference.put(word, oldCount+1);
        }
        else
            mapReference.put(word, 1);
    }

    // given a file, return list of all file data with precision, accuracy
    public ObservableList<TestFile> processTestFolder(File file) throws IOException
    {

        ObservableList<TestFile> files = FXCollections.observableArrayList();
        File[] folders = file.listFiles();

        for (File folder: folders) {
            if (folder.isDirectory()) {
                String fileName = folder.getName();
                String actualClass;

                // Keep track of folder type
                if (fileName.contains("ham")) {
                    actualClass = "ham";
                } else {
                    actualClass = "spam";
                }

                File[] contents = folder.listFiles();
                for (File current : contents) {

                    // Ignore cmds file
                    if(current.getName().contains("cmds"))
                        continue;


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

    // Check folders for training
    public void processTrainFolder(File file) throws IOException
    {
        File[] folders = file.listFiles();

        for (File folder: folders) {
            if (folder.isDirectory()) {
                String folderName = folder.getName();

                // Unsafe Alternative
                //processContents(folder, folderName.contains("ham") ? DataType.Ham : DataType.Spam);

                if(folderName.contains("ham"))
                    processContents(folder, DataType.Ham);
                else if(folderName.contains("spam"))
                    processContents(folder, DataType.Spam);

            }
        }
    }

    // Acquire all words from files from a folder and add them to their corresponding map (TRAINING)
    public void processContents(File folder, DataType data) throws IOException
    {
        File[] contents = folder.listFiles();
        for (File current : contents) {

            if (current.exists()) {

                // Ignore cmds file
                if (current.getName().contains("cmds"))
                    continue;

                if (data == DataType.Ham)
                    hamFiles++;
                else
                    spamFiles++;


                Scanner scanner = new Scanner(current);
                scanner.useDelimiter("[\\s\\.;:\\?\\!,]");
                while (scanner.hasNext()) {
                    String word = scanner.next();
                    word = word.toLowerCase();
                    if (isWord(word)) {
                        addWordToMap(word, data);
                    }
                }
            }
        }
    }



    // Returns spam probability of file (training is assumed to be complete)
    public Double testFile(File file) throws IOException
    {
        // Error Exception
        if(spamFiles == 0 || hamFiles == 0)
            return 0.0;

        Double n = 0.0;

        if(file.exists())
        {
            Scanner scanner = new Scanner(file);
            scanner.useDelimiter("[\\s\\.;:\\?\\!,]");
            while(scanner.hasNext())
            {
                String word = scanner.next();
                word = word.toLowerCase();

                // Check Word
                if(isWord(word)) {

                    // Check if result is stored already
                    if(ratioCounts.containsKey(word))
                    {
                        //System.out.println(ratioCounts.get(word));
                        n += ratioCounts.get(word);
                    }
                    // Else, If word is discovered in both maps, calculate probability
                    else if (spamCounts.containsKey(word) && hamCounts.containsKey(word))
                    {
                        Double PSW, PWS, PWH;
                        Double PSW_B;
                        int Count;
                        int SpamWords = spamCounts.get(word);
                        int HamWords = hamCounts.get(word);

                        PWS = SpamWords / spamFiles;
                        PWH = HamWords / hamFiles;
                        PSW = PWS / (PWS + PWH);

                        Double Result = 0.0;

                        if (PSW < 0.5) {
                            // Alternate Method for probability that ends up lowering accuracy much more than its increases in precision
                            Count = SpamWords + HamWords;
                            PSW_B = (3 * (0.5) + Count * PSW) / (3 + Count);
                            Result = Math.log(1 - PSW_B) - Math.log(PSW_B);
                        } else {
                            Result = Math.log(1 - PSW) - Math.log(PSW);
                        }

                        // Add already calculated result
                        ratioCounts.put(word, Result);

                        // Add value
                        n += Result;
                    }
                }
            }
        }

        Double PSF = 1/(1+Math.pow(Math.E, n));
        return PSF;
    }

    private int TotalCommonWords;
    private List<String> CommonWords_List;
    private String[] CommonWords_Array =
            {
                    "a",
                    "aboard",
                    "about",
                    "above",
                    "absent",
                    "according",
                    "across",
                    "after",
                    "against",
                    "ahead",
                    "although",
                    "along",
                    "alongside",
                    "amid",
                    "amidst",
                    "among",
                    "and",
                    "anti",
                    "around",
                    "as",
                    "at",
                    "atop",
                    "because",
                    "before",
                    "behind",
                    "below",
                    "between",
                    "but",
                    "by",
                    "considering",
                    "down",
                    "during",
                    "except",
                    "excluding",
                    "following",
                    "for",
                    "from",
                    "if",
                    "in",
                    "is",
                    "inside",
                    "into",
                    "like",
                    "mid",
                    "near",
                    "next",
                    "nor",
                    "now",
                    "of",
                    "off",
                    "on",
                    "once",
                    "onto",
                    "opposite",
                    "or",
                    "outside",
                    "over",
                    "past",
                    "per",
                    "plus",
                    "prior",
                    "regarding",
                    "round",
                    "save",
                    "since",
                    "so",
                    "some",
                    "than",
                    "that",
                    "the",
                    "though",
                    "through",
                    "throughout",
                    "to",
                    "unless",
                    "until",
                    "up",
                    "when",
                    "where",
                    "whether",
                    "while",
                    "with",
                    "within",
                    "without"
            };
}
