/**
 * TimesTables program implements an application that produces visual animation by representing times tables as a circle
 * @author: Mausam Shrestha
 * @Date: 2/4/2021
 * @Project: TimesTables
 * @Course: CS351
 * @UNM-ID: 101865530
 */
package sample;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;

public class TimesTables extends Application {

    /* Our scene and graphics context */
    private Scene mainScene;
    private GraphicsContext gc;

    /* timeline to record keyframes on timed sequence */
    private Timeline timeline;

    /* incase we want to change colors while animation is running, keeps track of the times tables and multiplier */
    private double timesTable;
    private int multiplier = 0;

    /* Our array to hold the positions of our points */
    private final double[] XCOOR = new double[360];
    private final double[] YCOOR = new double[360];

    /* labels that get updated frequently */
    private Label speedLabel;
    private Label incLabel;
    private Slider incSlider;
    private Slider speedSlider;

    /* Our colors scheme and favourite pictures */
    ArrayList<Color> colors;
    private ComboBox<String> favourites;

    /* Our text field and increment value tracker */
    private TextField goTo;
    private TextField points;
    private double increment = 0;

    /* Our alert dialog */
    private final Alert warning = new Alert(Alert.AlertType.WARNING);

    /* variables to keep track of activities */
    private boolean playing = false;
    private boolean finish = false;
    private boolean display = false;
    private boolean slide = false;
    private boolean select = false;
    private boolean pause = false;

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * start - override method of superclass, gets called when our application starts
     * @param primaryStage - our main window
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        /*
         * Sets up the Color and GUI components
         */
        colorSet();
        setup();

        /*
         * Sets up the window
         */
        primaryStage.setScene(mainScene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    /**
     * setup - sets up the GUI components and their functionality.
     * Arranges the GUI components in a Borderlayout
     * Sets up the scene for the window
     */
    public void setup() {

        /*
         * Canvas for drawing and performing our circle animations
         */
        Canvas myCanvas = new Canvas(700, 700);
        gc = myCanvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.strokeOval(45, 65, 600, 600);
        gc.setLineWidth(0.5);

        /*
         * Top region of Borderpane
         * Includes a Vbox
         * Includes timesLabel and color change button inside Vbox
         * Positioned center and aligned vertically
         */

        Label timesTables = new Label("Times Tables");
        timesTables.setTextFill(Color.GRAY);
        timesTables.setFont(new Font("Calibri", 40));

        Button change = new Button("Color");
        change.setPrefSize(100, 30);
        change.setOnAction(event -> {
            if (select) {
                int multiplicand = 0;

                /* Utilized the value in favourites to change the color, by repainting*/
                switch (favourites.getValue()) {
                    case "97/360":
                        multiplicand = 97;
                        break;
                    case "111/360":
                        multiplicand = 111;
                        break;
                    case "122/360":
                        multiplicand = 122;
                        break;
                    case "199/360":
                        multiplicand = 199;
                        break;
                    case "300/360":
                        multiplicand = 300;
                        break;
                    case "123/360":
                        multiplicand = 123;
                        break;
                    case "73/360":
                        multiplicand = 73;
                        break;
                    case "59/360":
                        multiplicand = 59;
                        break;
                    case "350/360":
                        multiplicand = 350;
                        break;
                    case "251/360":
                        multiplicand = 251;
                        break;
                }
                changeColor(multiplicand,360,360);
            }
            else if(display) {
                /* specializing change color when the screen is in display or slide mode */
                changeColor(Double.parseDouble(goTo.getText()), Integer.parseInt(points.getText()), Integer.parseInt(points.getText()));
            }
            else if (notEmpty()) changeColor(Integer.parseInt(goTo.getText()),multiplier, Integer.parseInt(points.getText()));
        });

        VBox topBox = new VBox(timesTables, change);
        topBox.setSpacing(40);
        topBox.setPadding(new Insets(30, 0, 0, 0));
        topBox.setAlignment(Pos.CENTER);

        /*
         * Bottom region of Borderpane
         * Includes 1 Vbox and 2 Hbox
         * Both Hbox are aligned vertically inside Vbox
         * Includes Play, Pause, Reset button on the top Hbox
         * Includes of Jumpto, multiplicand text field and points text field in the lower Hbox
         */

        Button reset = new Button("Reset");
        reset.setPrefSize(60, 30);
        reset.setOnAction(event -> {

            /* reset if the screen is being utilized */
            if(playing || display || select || pause ) hardReset();
            playing = false;
            display = false;
            pause = false;
            select = false;

        });

        Button start = new Button("PLAY");
        start.setPrefSize(60, 30);
        start.setOnAction(event -> {
            if (!finish && !display && !playing && !slide && notEmpty()) {

                /* don;t need to reset the circle if the animation is paused */
                if(!pause) refreshScreen();
                pause = false;
                animate();
                display = false;
                slide = false;
                select = false;
                playing = true;
                timeline.play();
                finish = true;
            }
            else if (display && notEmpty() && (incSlider.getValue() >= 0.1)){
                /* Settings and specialization when the user is watching the slides by incrementing slider */
                slide = true;
                increment = increment + incSlider.getValue();
                goTo.setText(String.valueOf(increment));
                refreshScreen();
                displayNext(increment, Integer.parseInt(points.getText()));
            }
            else if (!select && !playing && !finish){
                warning.setTitle("Illegal action intended!");
                if(!display) warning.getDialogPane().setContentText("Enter a valid points value 0 < int < 360");
                else {
                    warning.getDialogPane().setContentText("Increase the increment slide or reset the screen to " + "play " + "animation ");}
                    warning.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                    warning.show();
                }
        });

        Button stop = new Button("Pause");
        stop.setPrefSize(60, 30);
        stop.setOnAction(event -> {
            /* pause if not playing */
            if (playing){
                pause = true;
                playing = false;
                finish = false;
                select = false;
                timeline.pause();
            }
        });

        Button jumpTo = new Button("Jump To");
        jumpTo.setPrefSize(100, 30);
        jumpTo.setAlignment(Pos.CENTER);
        jumpTo.setOnAction(event -> {
            /* jump to the given Times table picture */
            if(!goTo.getText().isEmpty() && !points.getText().isEmpty()) {
                select = false;
                display = true;
                refreshScreen();
                displayNext(Double.parseDouble(goTo.getText()), Integer.parseInt(points.getText()));
            }
        });

        goTo = new TextField("");
        goTo.setFont(new Font("Arial", 20));
        goTo.setMaxWidth(70);
        goTo.setMaxHeight(20);
        goTo.setAlignment(Pos.BOTTOM_LEFT);

        points = new TextField();
        points.setFont(new Font("Arial", 20));
        points.setMaxWidth(70);
        points.setMaxHeight(20);
        points.setAlignment(Pos.BOTTOM_LEFT);

        Button noOfPoints = new Button("Total points");
        noOfPoints.setPrefSize(100,30);
        noOfPoints.setAlignment(Pos.CENTER);
        noOfPoints.setOnAction(event -> {
        });


        HBox bottomBox = new HBox(8);
        bottomBox.setPadding(new Insets(20, 0, 20, 0));
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.getChildren().addAll(reset, start, stop);

        HBox bottomLayer = new HBox();
        bottomLayer.setPadding(new Insets(0, 0, 10, 0));
        bottomLayer.setAlignment(Pos.CENTER);
        bottomLayer.setSpacing(10);
        bottomLayer.getChildren().addAll(jumpTo, goTo, noOfPoints,points);

        VBox arrangeBottom = new VBox();
        arrangeBottom.setAlignment(Pos.CENTER);
        arrangeBottom.getChildren().addAll(bottomBox, bottomLayer);

        /*
         * Left and Right  region of Borderpane
         * has inc slider on the left and speed slider on the right
         * has combo box for favourites in the left bottom of the Border pane
         */

        speedLabel = new Label("5");
        speedLabel.setTextFill(Color.BLACK);
        speedLabel.setFont(new Font("Arial", 20));

        incLabel = new Label("0.0");
        incLabel.setTextFill(Color.BLACK);
        incLabel.setFont(new Font("Arial", 20));

        incSlider = new Slider(0, 5, 0);
        incSlider.setOrientation(Orientation.VERTICAL);
        incSlider.setPrefHeight(500);
        incSlider.setShowTickLabels(true);
        incSlider.setShowTickMarks(true);
        incSlider.setMajorTickUnit(0.1);
        incSlider.setBlockIncrement(0.1);
        incSlider.valueProperty().addListener((observable, oldValue, newValue) ->{
            incLabel.textProperty().setValue((String.valueOf(newValue.doubleValue())).substring(0,3));
        });

        speedSlider = new Slider(0, 10, 5);
        speedSlider.setOrientation(Orientation.VERTICAL);
        speedSlider.setPrefHeight(500);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(1);
        speedSlider.setBlockIncrement(1);
        speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            speedLabel.textProperty().setValue(String.valueOf(newValue.intValue()));
            if (playing) timeline.setRate(speedSlider.getValue() / 5);
        });

        Label incLabel = new Label("INC");
        incLabel.setTextFill(Color.GRAY);
        incLabel.setPadding(new Insets(0,0,40,0));
        incLabel.setFont(new Font("Arial",20));

        Label speed = new Label("SPD");
        speed.setTextFill(Color.GRAY);
        speed.setPadding(new Insets(0,0,100,0));
        speed.setFont(new Font("Arial",20));

        /* Setting up favourites */
        favourites = new ComboBox<>();
        favourites.setPromptText("Favourites");
        favourites.getItems().add("59/360");
        favourites.getItems().add("73/360");
        favourites.getItems().add("97/360");
        favourites.getItems().add("111/360");
        favourites.getItems().add("122/360");
        favourites.getItems().add("123/360");
        favourites.getItems().add("199/360");
        favourites.getItems().add("251/360");
        favourites.getItems().add("300/360");
        favourites.getItems().add("350/360");
        favourites.setOnAction(event -> {
            select = true;
            /*  if the animation is playing stop it before we display our favourite pictures */
            if(playing) {
                timeline.stop();
                timeline.getKeyFrames().removeAll();
            }
            display = false;
            playing = false;
            refreshScreen();

            /* Switch cases to display the required Times Table picture from our favourites */
            switch (favourites.getValue()) {
                case "59/360":
                    displayNext(59,360);
                    break;
                case "73/360":
                    displayNext(73,360);
                    break;
                case "97/360":
                    displayNext(97, 360);
                    break;
                case "123/360":
                    displayNext(123,360);
                    break;
                case "111/360":
                    displayNext(111, 360);
                    break;
                case "122/360":
                    displayNext(122, 360);
                    break;
                case "199/360":
                    displayNext(199, 360);
                    break;
                case "251/360":
                    displayNext(251,360);
                    break;
                case "300/360":
                    displayNext(300, 360);
                    break;
                case "350/360":
                    displayNext(350, 360);
                    break;
            }
        });

        VBox leftBox = new VBox(this.incLabel, incSlider, incLabel, favourites);
        leftBox.setSpacing(20);
        leftBox.setPadding(new Insets(0, 40, 0, 40));
        leftBox.setAlignment(Pos.TOP_LEFT);

        VBox rightBox = new VBox(speedLabel, speedSlider, speed);
        rightBox.setSpacing(20);
        rightBox.setPadding(new Insets(0, 40, 0, 40));
        rightBox.setAlignment(Pos.CENTER);

        /* Adds the arrangements we made to our Borderpane layout */
        BorderPane layout = new BorderPane();
        layout.setCenter(myCanvas);
        layout.setTop(topBox);
        layout.setBottom(arrangeBottom);
        layout.setRight(rightBox);
        layout.setLeft(leftBox);
        layout.setBackground(Background.EMPTY);

        /* Set the scene with our custom Borderpane layout */
        mainScene = new Scene(layout, 1000, 1000, Color.WHITE);
    }

    /**
     * animate - Generates the timed sequence of key frames for our circle animation
     * Utilizes timeline to generate the sequence and records it in the timeline
     */
    private void animate() {
        int totalPoints;
        timeline = new Timeline();

        if(notEmpty()) {
            totalPoints = Integer.parseInt(points.getText());
            distributePoints(Integer.parseInt(points.getText()), 300, 345, 365);
            for (int i = 0; i < totalPoints; i++) {
                /* Generate key frames that paint on the canvas, time it, and add it to the timeline */
                timeline.getKeyFrames().add(new KeyFrame(Duration.millis(50 + i * 50), event -> paint(totalPoints)));
            }
        }
    }


    /**
     * paint - Strokes lines according to the value of timestables
     * @param totalPoints: Total points around the circle
     */
    public void paint(int totalPoints) {
        /* Utilized by timeline to time these painting sequence by sequence */
        if(!display){
            timesTable = Integer.parseInt(goTo.getText()) * multiplier;
            while (!(timesTable < totalPoints)) {
                timesTable = timesTable - totalPoints;
                if (timesTable == totalPoints) timesTable = 0;
            }
            gc.strokeLine(XCOOR[multiplier], YCOOR[multiplier], XCOOR[(int) timesTable], YCOOR[(int) timesTable]);
            multiplier++;
            if (multiplier >= totalPoints) timeline.stop();
        }
    }

    /**
     * distributePoints - Distributes given number of points equidistantly around the circumference of our circle
     * @param numberOfPoints: required number of points
     * @param radius: radius of the circle
     * @param centerX: x-coordinate of the center of our circle
     * @param centerY: y-coordinate of the center of our circle
     */
    public void distributePoints(int numberOfPoints, double radius, double centerX, double centerY) {
        double angle;
        for (int i = 1; i <= numberOfPoints; i++) {
            /* Distribute points equidistantly */
            angle = ((Math.PI * 2) / numberOfPoints) * i;

            /* Record the positions of our points distributed around the circle */
            XCOOR[i - 1] = (radius * (-Math.cos(angle))) + centerX;
            YCOOR[i - 1] = (radius * Math.sin(angle)) + centerY;
        }
    }

    /**
     * hardReset - Reset all values and set up for a brand new cycle
     */
    public void hardReset() {
        gc.setFill(Color.WHITE);
        gc.fillOval(45, 65, 600, 600);
        if(!display && !select) {
            timeline.getKeyFrames().removeAll();
            timeline.stop();
        }

        /* Reset values */
        finish = false;
        playing = false;
        select = false;
        slide = false;
        display = false;
        multiplier = 0;
        timesTable = 0;
        increment = 0;
        incSlider.setValue(0);
        points.setText(null);
        goTo.setText(null);
        speedSlider.setValue(5);
    }

    /**
     * refreshScreen - fresh new blank circle on the screen
     */
    public void refreshScreen(){

        /* Reset the circle */
        gc.setFill(Color.WHITE);
        gc.fillOval(45, 65, 600, 600);
    }

    /**
     * colorSet - Sets up an Arraylist of colors
     */
    public void colorSet() {
        /* Setup our colors */
        colors = new ArrayList<>(10);
        colors.add(Color.GRAY);
        colors.add(Color.rgb(0, 93, 250));
        colors.add(Color.rgb(188, 42, 141));
        colors.add(Color.BLACK);
        colors.add(Color.rgb(102, 255, 0));
        colors.add(Color.rgb(5, 188, 230));
        colors.add(Color.rgb(255, 17, 0));
        colors.add(Color.rgb(255, 165, 0));
        colors.add(Color.rgb(102, 51, 153));
        colors.add(Color.rgb(236, 71, 233));
    }

    /**
     * changeColor - changes the color of the circle
     * @param multiplicand:
     * @param cap: The max count for loop when the circle is animating, in other case just the total no.of points
     * @param points: Total points around the circle
     */
    public void changeColor(double multiplicand, int cap, int points) {

        double multiply;

        refreshScreen();
        gc.setStroke(colors.get((int) (Math.random() * 10)));
        gc.strokeOval(45, 65, 600, 600);

        for (int i = 0; i < cap; i++) {
            multiply = multiplicand * i;
            while (!(multiply < points)) {
                multiply = multiply - points;
                if (multiply == points) multiply = 0;
            }
            gc.strokeLine(XCOOR[i], YCOOR[i], XCOOR[(int) multiply], YCOOR[(int) multiply]);
        }
    }

    /**
     * displayNext- displays the result of the circle associated with jumpTo
     * @param jumpTo: Used to jump to the picture of the circle of the given number.
     * @param totalPoints: Total points around the circle
     */
    public void displayNext(double jumpTo, int totalPoints) {
        double tablePixel;
        if((jumpTo <= 360 && totalPoints <= 360) || select){
            if(increment == 0 && !select) {
                increment = Double.parseDouble(goTo.getText());
            }
            /* animation is not playing */
            playing = false;
            distributePoints(totalPoints, 300, 345, 365);

            /* paint the Times tables representation */
            for (int i = 0; i < totalPoints; i++) {
                tablePixel = i * jumpTo;

                while (!(tablePixel < totalPoints)){
                   tablePixel = tablePixel - totalPoints;
                   if(tablePixel == totalPoints) tablePixel = 0;
               }
            gc.strokeLine(XCOOR[i], YCOOR[i], XCOOR[(int) tablePixel], YCOOR[(int) tablePixel]);
           }
       }
        else{
            /* error dialog */
            warning.setTitle("Illegal action intended!");
            warning.getDialogPane().setContentText("Enter a value <= 360");
            warning.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            warning.show();
        }
    }

    /**
     * emptyFields - Checks if the Jump to and Points text field are empty.
     * @return returns true if the fields are empty , else returns false
     */
    public boolean notEmpty(){
        try{
            if(goTo.getText().isEmpty() || points.getText().isEmpty()) return false;
        }
        catch (NullPointerException e) {
            return false;
        }
        return true;
    }
}
