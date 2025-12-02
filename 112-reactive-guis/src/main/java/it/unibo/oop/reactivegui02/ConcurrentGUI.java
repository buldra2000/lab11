package it.unibo.oop.reactivegui02;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.oop.JFrameUtil;

import java.io.Serial;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Second example of reactive GUI.
 */
public final class ConcurrentGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentGUI.class);
    private JLabel display = new JLabel();
    final JButton up_counter;
    final JButton down_counter;
    final JButton stop_counter;

    public ConcurrentGUI(){
        super();
        JFrameUtil.dimensionJFrame(this);
        final JPanel panel = new JPanel();
        panel.add(display);
        up_counter = new JButton("up");
        down_counter = new JButton("down");
        stop_counter = new JButton("stop");
        panel.add(up_counter);
        panel.add(down_counter);
        panel.add(stop_counter);
        this.getContentPane().add(panel);
        this.setVisible(true);

        final Agent agent = new Agent();

        ExecutorService exec = Executors.newSingleThreadExecutor();
        exec.submit(agent);

        up_counter.addActionListener(e -> agent.countUp());
        down_counter.addActionListener(e -> agent.countDown());
        stop_counter.addActionListener(e -> agent.countStop());

    }

    private final class Agent implements Runnable{

        private volatile boolean stop;
        private volatile int countingDown;
        private int counter;

        @Override
        public void run() {
            while(!this.stop){
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> ConcurrentGUI.this.display.setText(nextText));

                    if(countingDown == 0){
                        this.counter++;
                    }
                    if(countingDown == 1){
                        this.counter--;
                    }
                    if (countingDown == -1){
                        stop = true;
                    }

                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        }

        public void countDown(){
            countingDown = 1;
        }

        public void countUp(){
            countingDown = 0;
        }

        public void countStop(){
            countingDown = -1;
            stop_counter.setEnabled(false);
            up_counter.setEnabled(false);
            down_counter.setEnabled(false);
            
        }
    }
    
}
