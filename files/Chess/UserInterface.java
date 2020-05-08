import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class UserInterface {

    static JTextArea textArea;
    static JFrame canvas;


    public static void main(String[] args) {


        Object[] options = {"Host a new Game", "Join an Existing Game"};
        int n = JOptionPane.showOptionDialog(null,
                "Would you like to start as a server or as a client ?",
                "Choose", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

        if (n == -1)
            System.exit(0);
        if (n == 0) {

            GameState.server = true;
            RMI.turnOnNetworking(null);


            while (GameState.ClientFound == false) {


            }


        }
        if (n == 1) {

            String ip = JOptionPane.showInputDialog("Please enter the IP address of the host");
            GameState.server = false;
            RMI.turnOnNetworking(ip);
        }


        Location.initRows();
        makeUI();
        Board.createBoard();


    }

    /**
     * Makes the main Chess UserInterface
     */

    static void makeUI() {


        canvas = new JFrame("Chess");
        canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        canvas.setSize(500, 500);
        canvas.setLocationRelativeTo(null);

        GUI_ChessPanel Pu = new GUI_ChessPanel();

        Pu.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));
        Pu.setVisible(true);

        canvas.add(Pu, BorderLayout.CENTER);


        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setText("");


        canvas.add(textArea, BorderLayout.PAGE_END);
        canvas.add(textArea, BorderLayout.SOUTH);


        canvas.setVisible(true);

        canvas.addWindowListener(new WindowListener() {

            @Override
            public void windowOpened(WindowEvent arg0) {
            }

            @Override
            public void windowIconified(WindowEvent arg0) {
            }

            @Override
            public void windowDeiconified(WindowEvent arg0) {
            }

            @Override
            public void windowDeactivated(WindowEvent arg0) {
            }

            @Override
            public void windowClosing(WindowEvent arg0) {
                GameState.exit(true);

            }

            @Override
            public void windowClosed(WindowEvent arg0) {
            }

            @Override
            public void windowActivated(WindowEvent arg0) {
            }
        });


    }

}

