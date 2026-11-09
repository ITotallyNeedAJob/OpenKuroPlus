package kat.respect.b2.injector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

final class InjectorGui {
   private final JFrame frame = new JFrame("Kuro+");
   private final DefaultListModel<RespectInjector.VmInfo> model = new DefaultListModel<>();
   private final JList<RespectInjector.VmInfo> list = new JList<>(this.model);
   private final JLabel status = new JLabel("Looking for running JVMs…");
   private final JButton injectButton = new JButton("Inject");
   private final JButton refreshButton = new JButton("Refresh");
   private volatile boolean busy;
   private Path agentJar;

   private InjectorGui() {
   }

   static boolean isAvailable() {
      try {
         if (GraphicsEnvironment.isHeadless()) {
            return false;
         } else {
            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            return !os.contains("linux") || System.getenv("DISPLAY") != null || System.getenv("WAYLAND_DISPLAY") != null;
         }
      } catch (Throwable var1) {
         return false;
      }
   }

   static void show() {
      if (isAvailable()) {
         SwingUtilities.invokeLater(() -> {
            try {
               UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Throwable var1) {
            }

            new InjectorGui().open();
         });
      }
   }

   private void open() {
      this.frame.setDefaultCloseOperation(3);
      this.frame.setLayout(new BorderLayout(0, 6));
      this.list.setSelectionMode(0);
      this.list.setFixedCellHeight(30);
      this.list.setFont(this.list.getFont().deriveFont(0, 13.0F));
      this.list.setCellRenderer(new InjectorGui.VmRenderer());
      this.list.addMouseListener(new MouseAdapter() {
         {
            Objects.requireNonNull(InjectorGui.this);
         }

         @Override
         public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2 && InjectorGui.this.list.getSelectedValue() != null) {
               InjectorGui.this.inject();
            }
         }
      });
      this.list.addListSelectionListener(e -> this.updateStatus());
      this.list.registerKeyboardAction(e -> this.inject(), KeyStroke.getKeyStroke(10, 0), 0);
      JLabel title = new JLabel("Choose the Minecraft process");
      title.setBorder(BorderFactory.createEmptyBorder(10, 12, 2, 12));
      title.setFont(title.getFont().deriveFont(1, 13.0F));
      this.status.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 8));
      this.status.setForeground(new Color(6316128));
      JPanel buttons = new JPanel(new FlowLayout(2, 6, 6));
      buttons.add(this.refreshButton);
      buttons.add(this.injectButton);
      JPanel south = new JPanel(new BorderLayout());
      south.add(this.status, "Center");
      south.add(buttons, "East");
      this.refreshButton.addActionListener(e -> this.refresh());
      this.injectButton.addActionListener(e -> this.inject());
      this.frame.getRootPane().setDefaultButton(this.injectButton);
      this.frame.getRootPane().registerKeyboardAction(e -> this.refresh(), KeyStroke.getKeyStroke(116, 0), 2);
      this.frame.add(title, "North");
      this.frame.add(new JScrollPane(this.list), "Center");
      this.frame.add(south, "South");
      this.frame.setSize(430, 300);
      this.frame.setMinimumSize(new Dimension(360, 220));
      this.frame.setLocationRelativeTo(null);
      this.resolveAgentJar();
      this.frame.setVisible(true);
      this.refresh();
   }

   private void resolveAgentJar() {
      try {
         this.agentJar = RespectInjector.resolveAgentJar();
      } catch (Throwable var2) {
         this.agentJar = null;
         this.injectButton.setEnabled(false);
         this.status.setForeground(new Color(11534336));
         this.status.setText(var2.getMessage());
      }
   }

   private void refresh() {
      if (!this.busy) {
         this.busy = true;
         this.refreshButton.setEnabled(false);
         this.status.setForeground(new Color(6316128));
         this.status.setText("Looking for running JVMs…");
         new Thread(() -> {
            List<RespectInjector.VmInfo> rows = new ArrayList<>();
            String error = null;

            try {
               for (RespectInjector.VmInfo vm : RespectInjector.describeJvms()) {
                  if (!vm.self()) {
                     rows.add(vm);
                  }
               }
            } catch (Throwable var5) {
               error = String.valueOf(var5);
            }

            String err = error;
            SwingUtilities.invokeLater(() -> {
               this.model.clear();

               for (RespectInjector.VmInfo vmx : rows) {
                  this.model.addElement(vmx);
               }

               this.busy = false;
               this.refreshButton.setEnabled(true);
               if (err != null) {
                  this.status.setForeground(new Color(11534336));
                  this.status.setText(err);
               } else if (rows.isEmpty()) {
                  this.status.setText("No JVMs found — start the game, then Refresh.");
               } else {
                  for (int i = 0; i < rows.size(); i++) {
                     if (rows.get(i).game()) {
                        this.list.setSelectedIndex(i);
                        break;
                     }
                  }

                  this.updateStatus();
               }
            });
         }, "kami-injector-scan").start();
      }
   }

   private void updateStatus() {
      RespectInjector.VmInfo vm = this.list.getSelectedValue();
      if (vm == null) {
         this.status.setText(this.model.isEmpty() ? "No JVMs found — start the game, then Refresh." : "Select a process.");
      } else {
         this.status.setForeground(new Color(6316128));
         this.status.setText(vm.game() ? "Minecraft detected — press Inject." : "Not recognised as Minecraft — press Inject anyway?");
      }
   }

   private void inject() {
      if (!this.busy && this.agentJar != null) {
         RespectInjector.VmInfo vm = this.list.getSelectedValue();
         if (vm == null) {
            this.status.setText("Select a process first.");
         } else {
            this.busy = true;
            this.injectButton.setEnabled(false);
            this.refreshButton.setEnabled(false);
            this.status.setForeground(new Color(6316128));
            this.status.setText("Injecting into " + vm.displayName() + " (pid " + vm.pid() + ")…");
            new Thread(
                  () -> {
                     String failure = null;

                     try {
                        RespectInjector.attachByPid(vm.pid(), null);
                     } catch (Throwable var4) {
                        failure = String.valueOf(var4);
                     }

                     String err = failure;
                     SwingUtilities.invokeLater(
                        () -> {
                           this.busy = false;
                           this.injectButton.setEnabled(true);
                           this.refreshButton.setEnabled(true);
                           if (err == null) {
                              this.status.setForeground(new Color(28672));
                              this.status.setText("Injected into pid " + vm.pid() + " — done.");
                              Timer close = new Timer(700, e -> {
                                 this.frame.dispose();
                                 System.exit(0);
                              });
                              close.setRepeats(false);
                              close.start();
                           } else {
                              this.status.setForeground(new Color(11534336));
                              this.status.setText("Failed: " + err);
                              JOptionPane.showMessageDialog(
                                 this.frame,
                                 "Could not inject into "
                                    + vm.displayName()
                                    + " (pid "
                                    + vm.pid()
                                    + ").\n\n"
                                    + err
                                    + "\n\nHints: the game must be your own process and run on a JDK; if it was already injected once, restart it and try again.",
                                 "Injection failed",
                                 0
                              );
                           }
                        }
                     );
                  },
                  "kami-injector-attach"
               )
               .start();
         }
      }
   }

   private static final class VmRenderer extends DefaultListCellRenderer {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
         super.getListCellRendererComponent(list, value, index, selected, focus);
         if (value instanceof RespectInjector.VmInfo vm) {
            this.setText((vm.game() ? "★  " : "     ") + vm.displayName() + "   ·  pid " + vm.pid());
            if (vm.game() && !selected) {
               this.setFont(this.getFont().deriveFont(1));
            }
         }

         return this;
      }
   }
}
