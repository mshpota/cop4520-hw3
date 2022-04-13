# COP4520 HOMEWORK 3

## PROBLEM 1:

#### IMPLEMENTATION

In this implementation, main method plays the role of Minotaur. Servants are implemented with ServantThread class that implements Runnable interface. Program starts as usually in main method. An additional space is used at the beginning to shuffle presents around. After that, those presents are stored with the help of ConcurrentLinkedDeque class. I decided to use it here for convenience, since it is not the focus of this assignment. I also added additional object of the same class to store the numbers that are pulled out of the shuffled bag by the servants to be able to use those numbers as the target elements for remove() method when writing "Thank you" notes. Minotaur creates and gets servants to work, it then proceeds to monitor how many thank you cards have been sent. This cards count is implemented with help of AtomicInteger class. When the count reaches the amount of presents, Minotaur stops the servants. Minotaur also with the help of ThreadLocalRandom class randomly makes a decision to submit a request for specific present. This request is implemented with AtomicInteger as well. 

Servants when started working check if Minotaur has a request, if he does they go and check in the ordered list. After that they randomly make a decision with use of ThreadLocalRandom class again. They decide either to write "Thank you" note or to pull a present from shuffled bag and add it to the ordered list. The cycle repeats until all "Thank you" notes were sent.

**Main part**:

For ordered list, originally I decided to implement using non-blocking list from the book[^1], since a lot of add and remove operations are being done in this program. This would speed up the process, if no locks need to be acquired at all. However, I wasn't able to get it to work properly. There was some problem with find() method that I could not solve. So, I then implemented using lazy list. Lazy list is good enough here, since it lets the threads traverse the list without using locks, and it uses locks only to lock two nodes before modifying the list. The use of additional marked variable in the Node class allows to have validate() and contains() methods that are wait-free.

**Times:**

1000 presents - 10 ms

10000 presents - 38 ms

100000 presents - 793 ms

500000 presents - 11613 ms

#### HOW TO RUN

To run this program, navigate to the [birthday-presents-party/](https://github.com/mshpota/cop4520-hw3/tree/main/birthday-presents-party) directory and type the following commands in the terminal window:

```
javac BirthdayPresentsParty.java

java BirthdayPresentsParty
```

## PROBLEM 2:

#### IMPLEMENTATION

**Times:**

#### HOW TO RUN

To run this program, navigate to the directory with the file and type the following commands in the terminal window:

```
javac .java

java 
```

[^1]: Herlihy, M., & Shavit, N. (2012). *The Art of Multiprocessor Programming,
 Revised Reprint* (1st ed., pp. 151-154). Morgan Kaufmann.