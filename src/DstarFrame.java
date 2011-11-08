//==================================================================================================
//
// DstarApp v.1 - To visualize the workings of the D* path search algorithm.
//
// Copyright (C) 2008  Leo Vandriel  (mail@leovandriel.com)
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
//==================================================================================================
package dstarapp;

import java.awt.Color;
import java.awt.event.MouseEvent;
import javax.swing.SpinnerNumberModel;

/** The main drawing window. */
public class DstarFrame extends javax.swing.JFrame implements Updateable
{

  private DstarGrid grid = null;
  private MapCanvas mapCanvas = null;
  private Position target = null;
  private Position attractor = null;
  private Position lastDragPosition = null;
  private Position lastMovePosition = null;
  private boolean button = true;
  private Position[][] brushes = new Position[][]
  {
    {
      new Position(0, 0)
    },
    {
      new Position(0, 0), new Position(-1, 0), new Position(1, 0), new Position(0, -1),
      new Position(0, 1)
    },
    {
      new Position(0, 0), new Position(-1, 0), new Position(1, 0), new Position(0, -1),
      new Position(0, 1), new Position(-1, -1), new Position(1, -1), new Position(-1, 1),
      new Position(1, 1)
    },
    {
      new Position(0, 0), new Position(-1, 0), new Position(1, 0), new Position(0, -1),
      new Position(0, 1), new Position(-1, -1), new Position(1, -1), new Position(-1, 1),
      new Position(1, 1), new Position(-2, 0), new Position(2, 0), new Position(0, -2),
      new Position(0, 2)
    }
  };

  /** Creates new form DstarFrame */
  public DstarFrame()
  {
    initComponents();

    // frame size
    setSize(800, 600);

    // init grid
    grid = new DstarGrid();
    sizeButton();
    connectionCombo();
    periodSpinner();
    fadeSpinner();
    flowSpinner();
    speedSlider();
    probSlider();
    threadBox();

    grid.addUpdateable(this);

    // init canvas
    mapCanvas = new MapCanvas(grid);
    canvasPanel.add(mapCanvas, java.awt.BorderLayout.CENTER);
    mapCanvas.addMouseListener(new java.awt.event.MouseAdapter()
    {

      @Override
      public void mousePressed(java.awt.event.MouseEvent evt)
      {
        canvasMousePressed(evt);
      }

      @Override
      public void mouseReleased(java.awt.event.MouseEvent evt)
      {
        canvasMouseReleased(evt);
      }
    });
    mapCanvas.addMouseMotionListener(new java.awt.event.MouseMotionAdapter()
    {

      @Override
      public void mouseDragged(java.awt.event.MouseEvent evt)
      {
        canvasMouseDragged(evt);
      }

      @Override
      public void mouseMoved(java.awt.event.MouseEvent evt)
      {
        canvasMouseMoved(evt);
      }
    });
  }

  private void canvasMouseMoved(java.awt.event.MouseEvent evt)
  {
    lastMovePosition = mapCanvas.parseMouse(evt.getX(), evt.getY());
    update();
  }

  private void canvasMouseDragged(java.awt.event.MouseEvent evt)
  {
    Position p = mapCanvas.parseMouse(evt.getX(), evt.getY());

    if (lastDragPosition != null && p != null)
    {
      int dx = lastDragPosition.x - p.x;
      int dy = lastDragPosition.y - p.y;

      int dxAbs = dx < 0 ? -dx : dx;
      int dyAbs = dy < 0 ? -dy : dy;

      if (dxAbs > 1 || dyAbs > 1)
      {
        if (dxAbs > dyAbs)
        {
          for (int i = 0; i < dxAbs; i++)
          {
            tileHit(p.x + i * dx / dxAbs, p.y + i * dy / dxAbs);
          }
        }
        else
        {
          for (int i = 0; i < dyAbs; i++)
          {
            tileHit(p.x + i * dx / dyAbs, p.y + i * dy / dyAbs);
          }
        }
      }
      else
      {
        tileHit(p.x, p.y);
      }
    }

    lastDragPosition = p;
  }

  private void canvasMouseReleased(java.awt.event.MouseEvent evt)
  {
    lastDragPosition = null;
    attractor.x = target.x;
    attractor.y = target.y;
  }

  private void canvasMousePressed(java.awt.event.MouseEvent evt)
  {
    button = evt.getButton() == MouseEvent.BUTTON1;
    Position p = mapCanvas.parseMouse(evt.getX(), evt.getY());
    lastDragPosition = p;

    if (p != null)
    {
      tileHit(p.x, p.y);
    }
  }

  private void tileHit(int mouseX, int mouseY)
  {

    if (setGoalRadio.isSelected())
    {
      if (button)
      {
        target.x = mouseX;
        target.y = mouseY;
        attractor.x = mouseX;
        attractor.y = mouseY;
      }
      else
      {
        attractor.x = mouseX;
        attractor.y = mouseY;
      }
    }

    if (editTerrainRadio.isSelected())
    {
      int brushIndex = brushCombo.getSelectedIndex();
      if (brushIndex >= 0 && brushIndex < brushes.length)
      {
        float speedRoot = 1 - speedSlider.getValue() / 100f;
        float speed = ((int) (speedRoot * speedRoot * 100) / 100f);
        for (int i = 0; i < brushes[brushIndex].length; i++)
        {
          int x = mouseX + brushes[brushIndex][i].x;
          int y = mouseY + brushes[brushIndex][i].y;
          if (button)
          {
            grid.setSpeed(x, y, speed);
          }
          else
          {
            grid.setSpeed(x, y, 1 - speed);
          }
        }
      }
    }

    mapCanvas.repaint();
  }

  public void update()
  {
    if (lastMovePosition == null)
    {
      positionLabel.setText("Move the pointer over");
      speedLabel.setText("a tile for info.");
      timeLabel.setText(" ");
      directionLabel.setText(" ");
    }
    else
    {
      Position p = lastMovePosition;

      float speed = grid.getSpeed(p.x, p.y);
      float time = grid.getTime(p.x, p.y);
      String direction = grid.getDirection(p.x, p.y);
      positionLabel.setText("Tile [" + p + "]");
      speedLabel.setText("Speed = " + speed);
      timeLabel.setText("Time = " + time);
      directionLabel.setText("Direction = " + direction);
    }
  }

  // -- buttons
  private void startStopButton()
  {

    if (mapCanvas == null)
    {
      return;
    }
    if (mapCanvas.running())
    {
      mapCanvas.stop();
      startStopButton.setText("Run");
    //sizeButton.setEnabled(true);
    }
    else
    {
      mapCanvas.start();
      startStopButton.setText("Pause");
    //sizeButton.setEnabled(false);
    }
  }

  private void stepButton()
  {

    if (mapCanvas == null)
    {
      return;
    }
    if (mapCanvas.running())
    {
      startStopButton();
    }
    mapCanvas.step();
  }

  private void sizeButton()
  {
    // parse fields
    int w = Integer.parseInt(widthField.getText());
    int h = Integer.parseInt(heightField.getText());

    if (w <= 2)
    {
      widthField.setText("3");
    }
    if (h <= 2)
    {
      heightField.setText("3");
    }
    if (w <= 2 || h <= 2)
    {
      return;
    }
    widthField.setText("" + w);
    heightField.setText("" + h);

    if (mapCanvas != null && mapCanvas.running())
    {
      startStopButton();
    }
    grid.setSize(w, h);

    target = new Position(w / 2, h / 2);
    attractor = new Position(w / 2, h / 2);

    grid.addTarget(target, attractor);

    //n8: grid.randomize(.45f);
    grid.randomize((probSlider.getValue() / 100f));

    if (mapCanvas != null)
    {
      mapCanvas.repaint();
    }
  }

  private void speedSlider()
  {
    float speedRoot = 1 - speedSlider.getValue() / 100f;
    float speed = ((int) (speedRoot * speedRoot * 100) / 100f);
    sliderLabel.setText("" + speed);
    fillButton.setBackground(Color.getHSBColor(0f, 0f, speedRoot));
  }

  private void probSlider()
  {
    probLabel.setText("p=" + (probSlider.getValue() / 100f));
  }

  private void fillButton()
  {
    float speedRoot = 1 - speedSlider.getValue() / 100f;
    float speed = ((int) (speedRoot * speedRoot * 100) / 100f);
    grid.setSpeed(speed);
    mapCanvas.repaint();
  }

  private void connectionCombo()
  {
    if (connectionCombo.getSelectedIndex() == 0)
    {
      grid.setConnection(DstarGrid.dir4);
    }
    else if (connectionCombo.getSelectedIndex() == 1)
    {
      grid.setConnection(DstarGrid.dir4diag);
    }
    else
    {
      grid.setConnection(DstarGrid.dir8);
    }
  }

  private void periodSpinner()
  {
    grid.period = (float) ((Double) periodSpinner.getValue()).doubleValue();
  }

  private void fadeSpinner()
  {
    float flow = (float) ((Double) flowSpinner.getValue()).doubleValue();
    float fade = (float) ((Double) fadeSpinner.getValue()).doubleValue();

    grid.fadeTime = fade;

    if (fade < flow)
    {
      flowSpinner.setValue(new Double(fade));
      grid.flowTime = fade;
    }
  }

  private void flowSpinner()
  {
    float flow = (float) ((Double) flowSpinner.getValue()).doubleValue();
    float fade = (float) ((Double) fadeSpinner.getValue()).doubleValue();

    grid.flowTime = flow;

    if (fade < flow)
    {
      fadeSpinner.setValue(new Double(flow));
      grid.fadeTime = flow;
    }
  }

  private void threadBox()
  {

    int newPriority = threadBox.isSelected() ? Thread.MIN_PRIORITY : Thread.NORM_PRIORITY;

    if (mapCanvas != null)
    {
      mapCanvas.setThreadPriority(newPriority);
    }
    Thread.currentThread().setPriority(newPriority);
  }

   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {
      java.awt.GridBagConstraints gridBagConstraints;

      radioGroup = new javax.swing.ButtonGroup();
      canvasPanel = new javax.swing.JPanel();
      jScrollPane1 = new javax.swing.JScrollPane();
      controlPanel = new javax.swing.JPanel();
      jPanel4 = new javax.swing.JPanel();
      jPanel8 = new javax.swing.JPanel();
      widthField = new javax.swing.JTextField();
      jLabel1 = new javax.swing.JLabel();
      heightField = new javax.swing.JTextField();
      jPanel12 = new javax.swing.JPanel();
      probSlider = new javax.swing.JSlider();
      probLabel = new javax.swing.JLabel();
      sizeButton = new javax.swing.JButton();
      jPanel6 = new javax.swing.JPanel();
      setGoalRadio = new javax.swing.JRadioButton();
      editTerrainRadio = new javax.swing.JRadioButton();
      jPanel1 = new javax.swing.JPanel();
      jLabel2 = new javax.swing.JLabel();
      jLabel4 = new javax.swing.JLabel();
      jLabel3 = new javax.swing.JLabel();
      stepButton = new javax.swing.JButton();
      startStopButton = new javax.swing.JButton();
      connectionCombo = new javax.swing.JComboBox();
      periodSpinner = new javax.swing.JSpinner();
      fadeSpinner = new javax.swing.JSpinner();
      flowSpinner = new javax.swing.JSpinner();
      threadBox = new javax.swing.JCheckBox();
      jPanel5 = new javax.swing.JPanel();
      jPanel10 = new javax.swing.JPanel();
      speedSlider = new javax.swing.JSlider();
      sliderLabel = new javax.swing.JLabel();
      brushCombo = new javax.swing.JComboBox();
      fillButton = new javax.swing.JButton();
      jPanel7 = new javax.swing.JPanel();
      positionLabel = new javax.swing.JLabel();
      speedLabel = new javax.swing.JLabel();
      timeLabel = new javax.swing.JLabel();
      directionLabel = new javax.swing.JLabel();

      setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
      setTitle("D* Demo");

      canvasPanel.setLayout(new java.awt.BorderLayout());
      getContentPane().add(canvasPanel, java.awt.BorderLayout.CENTER);

      jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      jScrollPane1.setMinimumSize(new java.awt.Dimension(200, 8));
      jScrollPane1.setPreferredSize(new java.awt.Dimension(200, 477));

      controlPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
         public void mouseMoved(java.awt.event.MouseEvent evt) {
            controlPanelMouseMoved(evt);
         }
      });
      controlPanel.setLayout(new java.awt.GridBagLayout());

      jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Grid Size"));
      jPanel4.setLayout(new java.awt.GridLayout(3, 1));

      jPanel8.setLayout(new javax.swing.BoxLayout(jPanel8, javax.swing.BoxLayout.LINE_AXIS));

      widthField.setText("50");
      jPanel8.add(widthField);

      jLabel1.setText(" x ");
      jLabel1.setInheritsPopupMenu(false);
      jPanel8.add(jLabel1);

      heightField.setText("50");
      jPanel8.add(heightField);

      jPanel4.add(jPanel8);

      jPanel12.setLayout(new javax.swing.BoxLayout(jPanel12, javax.swing.BoxLayout.LINE_AXIS));

      probSlider.setValue(35);
      probSlider.setPreferredSize(new java.awt.Dimension(80, 24));
      probSlider.addChangeListener(new javax.swing.event.ChangeListener() {
         public void stateChanged(javax.swing.event.ChangeEvent evt) {
            probSliderStateChanged(evt);
         }
      });
      jPanel12.add(probSlider);

      probLabel.setText("p=0.0");
      probLabel.setPreferredSize(new java.awt.Dimension(40, 14));
      jPanel12.add(probLabel);

      jPanel4.add(jPanel12);

      sizeButton.setText("Resize");
      sizeButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            sizeButtonActionPerformed(evt);
         }
      });
      jPanel4.add(sizeButton);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      controlPanel.add(jPanel4, gridBagConstraints);

      jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Mouse"));
      jPanel6.setLayout(new java.awt.GridLayout(2, 1));

      radioGroup.add(setGoalRadio);
      setGoalRadio.setSelected(true);
      setGoalRadio.setText("Move Goal");
      setGoalRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
      setGoalRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
      jPanel6.add(setGoalRadio);

      radioGroup.add(editTerrainRadio);
      editTerrainRadio.setText("Tile Brush");
      editTerrainRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
      editTerrainRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
      jPanel6.add(editTerrainRadio);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 2;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      controlPanel.add(jPanel6, gridBagConstraints);

      jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("D* Control"));
      jPanel1.setLayout(new java.awt.GridBagLayout());

      jLabel2.setText("Time Step (s) ");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 3;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      jPanel1.add(jLabel2, gridBagConstraints);

      jLabel4.setText("Fade Time (s)");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 4;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      jPanel1.add(jLabel4, gridBagConstraints);

      jLabel3.setText("Flow Time (s)");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 5;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      jPanel1.add(jLabel3, gridBagConstraints);

      stepButton.setText("Step");
      stepButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            stepButtonActionPerformed(evt);
         }
      });
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      jPanel1.add(stepButton, gridBagConstraints);

      startStopButton.setText("Run");
      startStopButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            startStopButtonActionPerformed(evt);
         }
      });
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      jPanel1.add(startStopButton, gridBagConstraints);

      connectionCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "4 Neighbors", "4 Diagnoal", "8 Neighbors" }));
      connectionCombo.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(java.awt.event.ItemEvent evt) {
            connectionComboItemStateChanged(evt);
         }
      });
      connectionCombo.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            connectionComboActionPerformed(evt);
         }
      });
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 2;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      jPanel1.add(connectionCombo, gridBagConstraints);

      periodSpinner.setModel(new javax.swing.SpinnerNumberModel(0.10000000149011612d, 0.01d, 10.0d, 0.01d));
      periodSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
         public void stateChanged(javax.swing.event.ChangeEvent evt) {
            periodSpinnerStateChanged(evt);
         }
      });
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 3;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      jPanel1.add(periodSpinner, gridBagConstraints);

      fadeSpinner.setModel(new SpinnerNumberModel(100f, 0f, 1000f, 1f));
      fadeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
         public void stateChanged(javax.swing.event.ChangeEvent evt) {
            fadeSpinnerStateChanged(evt);
         }
      });
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 4;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      jPanel1.add(fadeSpinner, gridBagConstraints);

      flowSpinner.setModel(new SpinnerNumberModel(100f, 0f, 1000f, 1f));
      flowSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
         public void stateChanged(javax.swing.event.ChangeEvent evt) {
            flowSpinnerStateChanged(evt);
         }
      });
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 5;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      jPanel1.add(flowSpinner, gridBagConstraints);

      threadBox.setSelected(true);
      threadBox.setText("Low Thread Priority");
      threadBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
      threadBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
      threadBox.addChangeListener(new javax.swing.event.ChangeListener() {
         public void stateChanged(javax.swing.event.ChangeEvent evt) {
            threadBoxStateChanged(evt);
         }
      });
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 6;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      jPanel1.add(threadBox, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      controlPanel.add(jPanel1, gridBagConstraints);

      jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Brush Properties"));
      jPanel5.setLayout(new java.awt.GridLayout(3, 1));

      jPanel10.setLayout(new javax.swing.BoxLayout(jPanel10, javax.swing.BoxLayout.LINE_AXIS));

      speedSlider.setValue(0);
      speedSlider.setPreferredSize(new java.awt.Dimension(50, 24));
      speedSlider.addChangeListener(new javax.swing.event.ChangeListener() {
         public void stateChanged(javax.swing.event.ChangeEvent evt) {
            speedSliderStateChanged(evt);
         }
      });
      jPanel10.add(speedSlider);

      sliderLabel.setText("1");
      sliderLabel.setPreferredSize(new java.awt.Dimension(40, 14));
      jPanel10.add(sliderLabel);

      jPanel5.add(jPanel10);

      brushCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1 Tile", "5 Tiles", "9 Tiles", "13 Tiles" }));
      jPanel5.add(brushCombo);

      fillButton.setText("Fill Grid");
      fillButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            fillButtonActionPerformed(evt);
         }
      });
      jPanel5.add(fillButton);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 3;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      controlPanel.add(jPanel5, gridBagConstraints);

      jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Tile Info"));
      jPanel7.setLayout(new java.awt.GridLayout(4, 1));

      positionLabel.setText(" ");
      jPanel7.add(positionLabel);

      speedLabel.setText(" ");
      jPanel7.add(speedLabel);

      timeLabel.setText(" ");
      jPanel7.add(timeLabel);

      directionLabel.setText(" ");
      jPanel7.add(directionLabel);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 8;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      controlPanel.add(jPanel7, gridBagConstraints);

      jScrollPane1.setViewportView(controlPanel);

      getContentPane().add(jScrollPane1, java.awt.BorderLayout.EAST);

      pack();
   }// </editor-fold>//GEN-END:initComponents

    private void threadBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_threadBoxStateChanged
      threadBox();
    }//GEN-LAST:event_threadBoxStateChanged

    private void periodSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_periodSpinnerStateChanged
      periodSpinner();
    }//GEN-LAST:event_periodSpinnerStateChanged

    private void fadeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fadeSpinnerStateChanged
      fadeSpinner();
    }//GEN-LAST:event_fadeSpinnerStateChanged

    private void flowSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_flowSpinnerStateChanged
      flowSpinner();
    }//GEN-LAST:event_flowSpinnerStateChanged

    private void probSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_probSliderStateChanged
      probSlider();
    }//GEN-LAST:event_probSliderStateChanged

    private void connectionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectionComboActionPerformed
      connectionCombo();
    }//GEN-LAST:event_connectionComboActionPerformed

    private void connectionComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_connectionComboItemStateChanged
    }//GEN-LAST:event_connectionComboItemStateChanged

    private void startStopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startStopButtonActionPerformed
      startStopButton();
    }//GEN-LAST:event_startStopButtonActionPerformed

    private void stepButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stepButtonActionPerformed
      stepButton();
    }//GEN-LAST:event_stepButtonActionPerformed

    private void sizeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sizeButtonActionPerformed
      sizeButton();
    }//GEN-LAST:event_sizeButtonActionPerformed

    private void controlPanelMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_controlPanelMouseMoved
      lastMovePosition = null;
      update();
    }//GEN-LAST:event_controlPanelMouseMoved

    private void speedSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_speedSliderStateChanged
      speedSlider();
    }//GEN-LAST:event_speedSliderStateChanged

    private void fillButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fillButtonActionPerformed
      fillButton();
    }//GEN-LAST:event_fillButtonActionPerformed

  public static void main(String args[])
  {
    java.awt.EventQueue.invokeLater(new Runnable()
    {

      public void run()
      {
        new DstarFrame().setVisible(true);
      }
    });
  }
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JComboBox brushCombo;
   private javax.swing.JPanel canvasPanel;
   private javax.swing.JComboBox connectionCombo;
   private javax.swing.JPanel controlPanel;
   private javax.swing.JLabel directionLabel;
   private javax.swing.JRadioButton editTerrainRadio;
   private javax.swing.JSpinner fadeSpinner;
   private javax.swing.JButton fillButton;
   private javax.swing.JSpinner flowSpinner;
   private javax.swing.JTextField heightField;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel3;
   private javax.swing.JLabel jLabel4;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JPanel jPanel10;
   private javax.swing.JPanel jPanel12;
   private javax.swing.JPanel jPanel4;
   private javax.swing.JPanel jPanel5;
   private javax.swing.JPanel jPanel6;
   private javax.swing.JPanel jPanel7;
   private javax.swing.JPanel jPanel8;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JSpinner periodSpinner;
   private javax.swing.JLabel positionLabel;
   private javax.swing.JLabel probLabel;
   private javax.swing.JSlider probSlider;
   private javax.swing.ButtonGroup radioGroup;
   private javax.swing.JRadioButton setGoalRadio;
   private javax.swing.JButton sizeButton;
   private javax.swing.JLabel sliderLabel;
   private javax.swing.JLabel speedLabel;
   private javax.swing.JSlider speedSlider;
   private javax.swing.JButton startStopButton;
   private javax.swing.JButton stepButton;
   private javax.swing.JCheckBox threadBox;
   private javax.swing.JLabel timeLabel;
   private javax.swing.JTextField widthField;
   // End of variables declaration//GEN-END:variables
}
