# Assignment 1 - CSCI2020u
Spencer Dowie - 100554393
Emmanuel Lajeunesse - 100547971

https://github.com/spencerdowie/A1

The Project is fully built already to a jar file so it does not need to be compiled

All of the data is located in the assignment1_data folder

Inhancements

We have an array of common words that can be ignored as the should appear equally in both spam and ham emails (SpamTrainer.java: ln 238, SpamTrainer.java: ln 41)

The probability of a word being in a spam email (PSW) is stored in a seperate map spo that it doesn't need to be re-computed ever time it appears in testing (SpamTrainer.java: ln 195)

We implemented a portion of Naive Bayes spam filtering which should help with potential over-weighting of words that rarely appear as we don't have enough data on them to make confident guesses (SpamTrainer.java: ln 215)

https://en.wikipedia.org/wiki/Naive_Bayes_spam_filtering#Dealing_with_rare_words
