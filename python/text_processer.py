import nltk
import asyncio
import websockets
from nltk.tokenize import word_tokenize
from nltk import pos_tag
from nltk import RegexpParser
from nltk.corpus import stopwords
from lexicalrichness import LexicalRichness
import matplotlib.pyplot as plt
import csv

nltk.download('punkt')
nltk.download('stopwords')
stop_words = set(
    stopwords.words('english'))  # creates stop words- words we don't want taking space, such as articles! -a


def processtext(text):
    sentence = (nltk.sent_tokenize(text))
    print(sentence)

    for sent in sentence:
        words = (nltk.word_tokenize(sent))  # creates word tokens from the existing sentence tokens -a

    filtered_sentence = [word for word in words if not word in stop_words]
    filtered_sentence = []

    for sent in sentence:
        words = (nltk.word_tokenize(sent))
        for wordindex in range(len(words)):
            if words[wordindex] not in stop_words:
                filtered_sentence.append(words[wordindex])
            if words[wordindex] == ".":
                filtered_sentence.remove(".")

    print(filtered_sentence)
    print(nltk.pos_tag(filtered_sentence))

    filename = "data.csv"
    rows = []
    with open(filename, 'w') as csvfile:
        for sent in sentence:
            templist = []
            lex = LexicalRichness(sent)
            wordcount = lex.words
            templist.append(wordcount)
            unique = lex.terms
            templist.append(unique)
            ttr = lex.ttr
            templist.append(ttr)
            rttr = lex.rttr
            templist.append(rttr)
            cttr = lex.cttr
            templist.append(cttr)
            rows.append(templist)
        fields = [wordcount, unique, ttr, rttr, cttr]
        csvwriter = csv.writer(csvfile)
        csvwriter.writerow(fields)
        csvwriter.writerows(rows)

    data = []
    with open("data.csv") as f:
        skip = True
        for line in f:
            if "NA" in line:
                skip = True
            if not skip:
                line = line.split(",")
                if len(line) == 5:
                    wordcount = line[0]
                    unique = line[1]
                    ttr = line[2]
                    rttr = line[3]
                    cttr = line[4]
                    data.append([wordcount, unique, ttr, rttr, cttr])
            else:
                skip = False

    wordcount = []
    unique = []
    ttr = []
    rttr = []
    cttr = []
    for row in data:
        row = [float(i) for i in row]
        wordcount.append(row[0])
        unique.append(row[1])
        ttr.append(row[2])
        rttr.append(row[3])
        cttr.append(row[4])

    plt.figure(1)
    plt.subplot(2, 3, 1)
    plt.bar(["Text " + str(i) for i in range(len(wordcount))], wordcount)
    plt.subplot(2, 3, 2)
    plt.bar(["Text " + str(i) for i in range(len(unique))], unique)
    plt.show()
    plt.subplot(2, 3, 2)
    plt.bar(["Text " + str(i) for i in range(len(ttr))], ttr)
    plt.subplot(2, 3, 2)
    plt.bar(["Text " + str(i) for i in range(len(rttr))], rttr)
    plt.subplot(2, 3, 2)
    plt.bar(["Text " + str(i) for i in range(len(cttr))], cttr)

    """plt.bar()
    plt.bar(2, wordcount)
    plt.bar(4,unique)
    plt.bar(6,ttr)
    plt.bar(8,rttr)
    plt.bar(10,cttr)
    plt.xlabel("Wordcount, Unique Words Said, TTR (Type Token Ratio), Root TTR, Corrected TTR ")
    plt.ylabel("Values")
    plt.title("Your Progress")
    plt.tick_params('x', labelsize=10, labelrotation=90) # handles bottom lables, otherwise we get overlap
    plt.show()"""





















