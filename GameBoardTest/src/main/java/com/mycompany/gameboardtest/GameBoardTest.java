/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.gameboardtest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.Map;
import static javax.swing.SwingConstants.CENTER;

public class GameBoardTest extends JFrame {

    /**
     * Constructor for the standalone test. Sets up the main window.
     */
    public GameBoardTest() {
        setTitle("GameBoard Standalone Test");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 800));
        setLocationRelativeTo(null);

        // Main panel for the game board
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(22, 33, 61)); // Equivalent to FrameConfig.BLUE
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(mainPanel);

        // Create player areas
        PlayerArea player1Area = new PlayerArea(true);
        PlayerArea player2Area = new PlayerArea(false);
        
        // Central play area (can be developed further)
        JPanel playArea = new JPanel();
        playArea.setOpaque(false);

        mainPanel.add(player1Area, BorderLayout.NORTH);
        mainPanel.add(playArea, BorderLayout.CENTER);
        mainPanel.add(player2Area, BorderLayout.SOUTH);
    }

    /**
     * Main method to launch the test application.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameBoardTest board = new GameBoardTest();
            board.setVisible(true);
        });
    }
}

/**
 * Represents a player's area on the board, containing health and card slots.
 */
class PlayerArea extends JPanel {
    private final HealthCircle healthCircle;
    private final Map<String, CardSlot> cardSlots = new HashMap<>();

    public PlayerArea(boolean isTopPlayer) {
        setOpaque(false);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        healthCircle = new HealthCircle();
        Color borderColor = isTopPlayer ? Color.GRAY : new Color(252, 163, 17); // Orange for bottom

        // Health Circle
        gbc.gridx = 4; // Centered
        gbc.gridy = isTopPlayer ? 0 : 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 0, 10, 0);
        add(healthCircle, gbc);

        // Panel for all the card slots, using BorderLayout for spacing
        JPanel cardsPanel = new JPanel(new BorderLayout(30, 0));
        cardsPanel.setOpaque(false);

        // Create and store all card slots
        CardSlot deckSlot = new CardSlot("Deck", borderColor);
        CardSlot discardSlot = new CardSlot("Discard", borderColor);
        CardSlot utilSlot = new CardSlot("Util", borderColor);

        cardSlots.put("Deck", deckSlot);
        cardSlots.put("Discard", discardSlot);
        cardSlots.put("Util", utilSlot);
        for (int i = 1; i <= 6; i++) {
            cardSlots.put("Field " + i, new CardSlot("", borderColor));
        }
        
        // Create a separate panel for the field and util slots with GridLayout
        JPanel fieldSlotsPanel = new JPanel(new GridLayout(1, 0, 10, 10));
        fieldSlotsPanel.setOpaque(false);
        
        for (int i = 1; i <= 3; i++) fieldSlotsPanel.add(cardSlots.get("Field " + i));
        fieldSlotsPanel.add(utilSlot);
        for (int i = 4; i <= 6; i++) fieldSlotsPanel.add(cardSlots.get("Field " + i));


        // Arrange card slots based on player position
        if (isTopPlayer) {
            cardsPanel.add(deckSlot, BorderLayout.WEST);
            cardsPanel.add(fieldSlotsPanel, BorderLayout.CENTER);
            cardsPanel.add(discardSlot, BorderLayout.EAST);
        } else { // Bottom player has a mirrored layout
            cardsPanel.add(discardSlot, BorderLayout.WEST);
            cardsPanel.add(fieldSlotsPanel, BorderLayout.CENTER);
            cardsPanel.add(deckSlot, BorderLayout.EAST);
        }
        
        gbc.gridx = 0;
        gbc.gridy = isTopPlayer ? 1 : 0;
        gbc.gridwidth = 9; // Span across all columns
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(cardsPanel, gbc);
    }
}

/**
 * A custom JPanel representing a single card slot on the board.
 */
class CardSlot extends JPanel {
    private final String slotName;
    private final Color borderColor;
    private boolean isHovered = false;
    private final Timer hoverTimer;
    private float glowAlpha = 0.0f;

    public CardSlot(String name, Color border) {
        this.slotName = name;
        this.borderColor = border;

        setPreferredSize(new Dimension(100, 150));
        setOpaque(false);

        hoverTimer = new Timer(20, e -> {
            glowAlpha += isHovered ? 0.05f : -0.05f;
            if (glowAlpha > 1.0f) glowAlpha = 1.0f;
            if (glowAlpha < 0.0f) {
                glowAlpha = 0.0f;
                ((Timer)e.getSource()).stop(); // Correctly stop the timer
            }
            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(
                    CardSlot.this,
                    "You clicked on: " + (slotName.isEmpty() ? "Field Slot" : slotName)
                );
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                if (!hoverTimer.isRunning()) hoverTimer.start();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int arc = 25;

        // Draw the border without filling the shape
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(new RoundRectangle2D.Float(1, 1, width - 2, height - 2, arc, arc));


        if (glowAlpha > 0) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glowAlpha));
            g2d.setColor(borderColor.brighter());
            for (int i = 0; i < 4; i++) {
                g2d.draw(new RoundRectangle2D.Float(i, i, width - 1 - i * 2, height - 1 - i * 2, arc, arc));
            }
        }
        
        g2d.setComposite(AlphaComposite.SrcOver);

        if (!slotName.isEmpty()) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g2d.getFontMetrics();
            int stringWidth = fm.stringWidth(slotName);
            g2d.drawString(slotName, (width - stringWidth) / 2, height / 2 + fm.getAscent() / 2);
        }

        g2d.dispose();
    }
}

/**
 * A custom JLabel that renders as a circle to display player health.
 */
class HealthCircle extends JLabel {
    private int health = 50;

    public HealthCircle() {
        setPreferredSize(new Dimension(80, 80));
        setForeground(Color.WHITE);
        setFont(new Font("Arial", Font.BOLD, 28));
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
        setText(String.valueOf(health));
        setOpaque(false);
    }

    public void setHealth(int newHealth) {
        this.health = newHealth;
        setText(String.valueOf(this.health));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        super.paintComponent(g);
        
        g2d.dispose();
    }
    
    @Override
    protected void paintBorder(Graphics g){
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.GRAY);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(new Ellipse2D.Float(1,1,getWidth()-2, getHeight()-2));
        g2d.dispose();
    }
}