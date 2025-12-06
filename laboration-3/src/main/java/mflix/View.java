package mflix;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

public class View extends JFrame {
    //JFrame
    private JTextArea resultText = new JTextArea(20, 2);
    private JTextArea inputText = new JTextArea(1, 10);
    //button
    private JButton fetchButton = new JButton("Sök");

    private JPanel topPanel = new JPanel(new BorderLayout());
    private JPanel bottomPanel = new JPanel(new FlowLayout());



    public View() {
        //Frame
        System.out.println("View Creating");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(300, 500);
        this.setResizable(false);
        this.setTitle("Hitta Filmer");
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout(10,10));

        // Komponent justeringar
        resultText.setEditable(false);
        //lägg till i panel
        topPanel.add(new JScrollPane(resultText), BorderLayout.CENTER);
        bottomPanel.add(inputText);
        bottomPanel.add(fetchButton);
        this.add(topPanel, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);

        this.setVisible(true);
    }
    public void showResults(ActionListener listener) {

        fetchButton.addActionListener(listener);
    }
    public String getInput() {
        return inputText.getText();
    }
    public void clearText() {
        inputText.setText("");
        resultText.setText("");
    }
    public void setResultText(String result) {
        resultText.append(result);
    }

}
