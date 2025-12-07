package mflix;

// Starta upp alla componenter i main
public class Main {
    static void main(String[] args) {
        Model model = new Model();
        model.initMongo();
        View view = new View();
        Controller control = new Controller(model, view);
        control.Listeners();
    }
}
