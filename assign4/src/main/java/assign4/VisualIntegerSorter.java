/* Name: Andrew Manga
** Date: March 24, 2023
** Class: ICS4U1 - J. Radulovic
** Assignment: Comparing Sorting Algorithms
** Purpose: Visualize the inner mechanisms of three separate sorting algorithms using JavaFX
*/

package assign4;

import java.util.*;
import javafx.application.*;
import javafx.beans.value.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;

public class VisualIntegerSorter extends Application {

    private int[] list;
    private int size = 64;
    private long seed = 2005;
    private long timer = 100; // in milliseconds

    final int WINDOW_WIDTH = 1920;
    final int WINDOW_HEIGHT = 1080;

    private BorderPane bp = new BorderPane();
    private XYChart.Series<String, Number> series;
    private ArrayList<Button> buttons = new ArrayList<Button>();
    private Button resetButton;

    public static void main(String[] args) {
        Application.launch(args);
    }

    public void start(Stage stage) {

        setList(generateList());

        HBox hbox = new HBox();
        hbox.setPadding(new Insets(10, 20, 10, 20));
        hbox.setSpacing(10);
        hbox.setAlignment(Pos.CENTER);
        hbox.setStyle("-fx-background-color: #cccccc;");

        Button button1 = new Button("Bubble Sort");
        button1.setOnAction(e -> sort_method1());

        Button button2 = new Button("Selection Sort");
        button2.setOnAction(e -> sort_method2());

        Button button3 = new Button("Merge Sort");
        button3.setOnAction(e -> sort_method3());

        Button randomizeButton = new Button("Randomize Seed");
        randomizeButton.setOnAction(e -> {
            seed = new Random().nextLong();
        });

        TextField sizeField = new TextField();
        sizeField.setPromptText("Array Size");
        sizeField.setPrefSize(80, 20);
        sizeField.textProperty().addListener(new ChangeListener<String>() { // this is triggered whenever the user types
                                                                            // something in the text field
            @Override
            public void changed(
                    ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {

                if (newValue.matches("\\d*")) { // if the input is a number then set the size to it
                    size = Integer.parseInt(newValue);
                } else {
                    sizeField.setText(oldValue);
                    sizeField.positionCaret(sizeField.getLength());
                }
            }
        });

        resetButton = new Button("Reset Array");
        resetButton.setDefaultButton(true);
        resetButton.setOnAction(e -> {
            setList(generateList());
            bp.setCenter(setupGraph());
            for (int i = 0; i < buttons.size(); i++)
                buttons.get(i).setDisable(false); // enable the rest of the buttons only when the array is reset
        });

        Label warningLabel = new Label("An array size > 150 is not recommended!");

        hbox.getChildren().addAll(button1, button2, button3, sizeField, randomizeButton, resetButton, warningLabel);
        buttons.addAll(Arrays.asList(button1, button2, button3, randomizeButton, resetButton));

        bp = new BorderPane();
        bp.setCenter(setupGraph());
        bp.setBottom(hbox);
        BorderPane.setMargin(hbox, new Insets(10));
        Scene scene = new Scene(bp);

        scene.setOnScroll((ScrollEvent event) -> {
            long minTimer = 10;
            long maxTimer = 500;
            long scrollSpeed = 10;
            double deltaY = event.getDeltaY();
            if (deltaY > 0) { // Meaning the user scrolled up
                if (timer > minTimer)
                    timer -= scrollSpeed;
            } else { // Meaning the user scrolled down
                if (timer < maxTimer)
                    timer += scrollSpeed;
            }
        });

        stage.setTitle("Visual Integer Sorter");
        stage.setScene(scene);
        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);
        stage.show();
    }

    public int[] generateList() {
        Random r = new Random();
        r.setSeed(seed);
        int[] array = new int[size];
        for (int i = 0; i < array.length; i++) {
            array[i] = i + 1; // so that the values begin from 1; otherwise there will be a blank space on the
                              // graph
        }
        for (int i = array.length - 1; i > 0; i--) { // Fisher-Yates shuffle
            int j = r.nextInt(i + 1);
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }

        return array;
    }

    public BarChart<String, Number> setupGraph() {

        String[] elements = new String[size]; // this needs to be a string array because of the category axis
        for (int n = 0; n < elements.length; n++) {
            elements[n] = Integer.toString(n);
        }

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setCategories(FXCollections.<String>observableArrayList(Arrays.asList(elements)));
        xAxis.setLabel("Element");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Value");

        BarChart<String, Number> barchart = new BarChart<String, Number>(xAxis, yAxis);
        barchart.setTitle("Sorting Algorithm Visualizer");
        barchart.setLegendVisible(false);
        barchart.setAnimated(false);
        barchart.setBarGap(0);

        series = new XYChart.Series<String, Number>();
        for (int i = 0; i < list.length; i++) {
            // add the values of all the elements of the list to the series
            series.getData().add(new XYChart.Data<String, Number>(Integer.toString(i), list[i]));
        }
        barchart.getData().add(series);

        return barchart;
    }

    public void setList(int[] list) {
        this.list = list.clone();
    }

    public int[] getList() {
        return list.clone();
    }

    public String toString() {
        return Arrays.toString(list).replaceAll("[\\[\\]]", "");
    }

    private void sort_method1() {
        Thread thread = new Thread(() -> {
            for (int i = 0; i < buttons.size(); i++)
                buttons.get(i).setDisable(true);

            long startTime = System.nanoTime();
            try {
                boolean flipped;
                int runlen = list.length;
                do {
                    flipped = false;
                    for (int i = 0; i < runlen - 1; i++) {
                        if (list[i] > list[i + 1]) {
                            swap(i, i + 1);

                            final int I = i;

                            Platform.runLater(() -> series.getData()
                                    .add(new XYChart.Data<String, Number>(Integer.toString(I), list[I])));
                            Platform.runLater(() -> series.getData()
                                    .add(new XYChart.Data<String, Number>(Integer.toString(I + 1), list[I + 1])));
                            Thread.sleep(timer);

                            flipped = true;
                        }
                    }
                    runlen--;
                } while (flipped);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long endTime = System.nanoTime();
            System.out
                    .println("Bubble sort runtime = " + (endTime - startTime) / 1e9 + "s at " + timer + "ms intervals");

            resetButton.setDisable(false);
        });
        thread.setDaemon(true); // so that this thread will be killed along with the main program
        thread.start();
    }

    private void sort_method2() {
        Thread thread = new Thread(() -> {
            for (int i = 0; i < buttons.size(); i++)
                buttons.get(i).setDisable(true);

            long startTime = System.nanoTime();
            try {
                for (int i = 0; i < list.length - 1; i++) {
                    for (int j = i + 1; j < list.length; j++) {
                        if (list[j] < list[i]) {
                            swap(j, i);

                            final int I = i;
                            final int J = j;
                            Platform.runLater(() -> series.getData()
                                    .add(new XYChart.Data<String, Number>(Integer.toString(I), list[I])));
                            Platform.runLater(() -> series.getData()
                                    .add(new XYChart.Data<String, Number>(Integer.toString(J), list[J])));
                            Thread.sleep(timer);
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long endTime = System.nanoTime();
            System.out.println(
                    "Selection sort runtime = " + (endTime - startTime) / 1e9 + "s at " + timer + "ms intervals");

            resetButton.setDisable(false);
        });
        thread.setDaemon(true); // so that this thread will be killed along with the main program
        thread.start();
    }

    private void sort_method3() {
        Thread thread = new Thread(() -> {
            for (int i = 0; i < buttons.size(); i++)
                buttons.get(i).setDisable(true);

            long startTime = System.nanoTime();
            try {
                int llen = list.length;
                int rows = (int) Math.ceil(Math.log(llen) / Math.log(2)) + 1;
                int[][][] arList = new int[rows][][];
                int pairs = llen;

                arList[0] = new int[llen][1];
                for (int i = 0; i < llen; i++) {
                    arList[0][i][0] = list[i];
                }
                for (int r = 1; r < rows; r++) {
                    pairs = (int) Math.ceil(pairs / 2.0);
                    arList[r] = new int[pairs][];
                    for (int p = 0; p < pairs; p++) {
                        if (p * 2 + 1 == arList[r - 1].length) {
                            arList[r][p] = arList[r - 1][p * 2];
                        } else {
                            arList[r][p] = combineArray(arList[r - 1][p * 2], arList[r - 1][p * 2 + 1]);

                            for (int i = 0; i < arList[r][p].length; i++) {
                                final int R = r;
                                final int P = p;
                                final int I = i;

                                Platform.runLater(() -> series.getData()
                                        .add(new XYChart.Data<String, Number>(
                                                Integer.toString(I + P * arList[R][P].length), arList[R][P][I])));
                                // P + arList[R][P].length represents the offset of the bars relative to the
                                // position in the array
                            }
                            Thread.sleep(timer);

                        }
                    }
                }

                list = arList[rows - 1][0];

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long endTime = System.nanoTime();
            System.out
                    .println("Merge sort runtime = " + (endTime - startTime) / 1e9 + "s at " + timer + "ms intervals");

            resetButton.setDisable(false);
        });
        thread.setDaemon(true); // so that this thread will be killed along with the main program
        thread.start();

    }

    private int[] combineArray(int[] array1, int[] array2) {
        int len1 = array1.length;
        int len2 = array2.length;
        int[] ret = new int[len1 + len2];
        int i = 0, i1 = 0, i2 = 0;

        while (i1 < len1 && i2 < len2) {
            if (array1[i1] < array2[i2]) {
                ret[i++] = array1[i1++];
            } else {
                ret[i++] = array2[i2++];
            }
        }

        for (; i1 < len1;) {
            ret[i++] = array1[i1++];
        }
        for (; i2 < len2;) {
            ret[i++] = array2[i2++];
        }

        return ret;
    }

    private void swap(int i, int j) {
        int temp = list[i];
        list[i] = list[j];
        list[j] = temp;
    }
}
