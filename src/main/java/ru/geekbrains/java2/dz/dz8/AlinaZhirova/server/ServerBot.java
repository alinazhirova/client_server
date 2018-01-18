package ru.geekbrains.java2.dz.dz8.AlinaZhirova.server;

import java.util.ArrayList;
import java.util.Random;

public class ServerBot {
    
    public class Question {
        private String question;
        private String answer;

        public Question(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }
    }


    private ArrayList<Question> questionList;
    private ArrayList<ClientHandler> alreadyAnswered;
    private MyServer server;
    private boolean active;
    private int questionNumber;
    private Random random;

    
    public ServerBot(MyServer server) {
        this.server = server;
        questionList = new ArrayList<Question>();
        questionList.add(new Question("What color is the grass?", "Green"));
        questionList.add(new Question("What season is the coldest?", "Winter"));
        questionList.add(new Question("Who says 'meow'?", "Cat"));
        questionList.add(new Question("What is the shape of the globe?", "Ball"));
        questionList.add(new Question("Whom do gentlemen prefer?", "Blondes"));

        random = new Random();
        alreadyAnswered = new ArrayList<ClientHandler>();
    }
    

    public void start() {
        if (!active) {
            Thread curThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    int time = 0;
                    while (active) {
                        try {
                            Thread.sleep(1000);
                            time++;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (time == 10) {
                            stop();
                        }
                    }
                }
            });
            curThread.setDaemon(true);
            curThread.start();
            active = true;
            questionNumber = random.nextInt(questionList.size());
            alreadyAnswered.clear();
            server.broadcastMsg("Quiz: " + questionList.get(questionNumber).question);
        }
    }

    
    public void stop() {
        if (active) {
            active = false;
            server.broadcastMsg("Quiz: no one was able to answer the question!");
            server.broadcastMsg("Quiz: the right answer is " + questionList.get(questionNumber).answer);
        }
    }

    
    public void tryToAnswer(ClientHandler curClient, String answer) {
        if (active) {
            if (!alreadyAnswered.contains(curClient)) {
                alreadyAnswered.add(curClient);
                if (answer.equalsIgnoreCase(questionList.get(questionNumber).answer)) {
                    server.broadcastMsg("Quiz: " + curClient.getName() + " correctly answered the question!");
                    active = false;
                } else {
                    server.broadcastMsg("Quiz: " + curClient.getName() + " was wrong");
                }
            } else {
                curClient.sendMsg("Quiz: you have already tried to answer the question...");
            }
        }
    }
    
    
}
