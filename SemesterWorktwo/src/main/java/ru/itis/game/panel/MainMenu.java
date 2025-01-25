package ru.itis.game.panel;

import ru.itis.game.panel.GamePanel;
import ru.itis.network.GameServer;
import ru.itis.cfg.ConfigValues;
import ru.itis.res.Colors;
import ru.itis.res.Strings;
import ru.itis.service.GameService;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;


public class MainMenu extends JPanel {

    private GameServer server;
    private final JCheckBox hostCheckBox;
    private final JTextField ipAddressField;
    private final JTextField nameField;
    private final GamePanel gamePanel;
    private final GameService gameService;
    public MainMenu(GamePanel gamePanel, GameService gameService) {
        this.gamePanel = gamePanel;
        this.gameService = gameService;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        hostCheckBox = new JCheckBox(Strings.HOST);
        hostCheckBox.setFocusable(false);
        hostCheckBox.setBackground(Colors.PANEL_MENU);
        add(hostCheckBox, gbc);


        gbc.gridy = 1;
        JLabel nameLabel = buildLabel(Strings.NAME);
        add(nameLabel, gbc);
        gbc.gridy = 2;
        nameField = new JTextField(11);
        nameField.setFocusable(true);
        add(nameField, gbc);

        gbc.gridy = 3;
        JLabel ipAddressLabel = buildLabel(Strings.IP_SAMPLE);
        add(ipAddressLabel, gbc);
        gbc.gridy = 4;
        ipAddressField = new JTextField(11);
        ipAddressField.setFocusable(true);
        add(ipAddressField, gbc);



        gbc.gridy = 5;
        JButton playButton = new JButton(Strings.TO_PLAY);
        playButton.setPreferredSize(new Dimension(120, 30));
        playButton.setFocusable(false);
        playButton.addActionListener(e -> startGame());
        add(playButton, gbc);

        setBackground(Colors.BACKGROUND_COLOR);
    }

    private JLabel buildLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Colors.TEXT_COLOR);
        return label;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();
        int panelWidth = 250;
        int panelHeight = 280;
        int x = (width - panelWidth) / 2;
        int y = (height - panelHeight) / 2;

        g.setColor(Colors.PANEL_MENU);
        g.fillRoundRect(x, y, panelWidth, panelHeight, 20, 20);
    }

    private void startGame() {
        String ipAddress = ipAddressField.getText();
        String playerName = nameField.getText();
        if (ipAddress.isBlank()) {
            ipAddress = ConfigValues.LOCALHOST;
        }
        if (playerName.isBlank() || playerName.length() > 10) {
            playerName = ConfigValues.DEFAULT_NAME;
        }
        gameService.setIpAddress(ipAddress);

        if (hostCheckBox.isSelected() && server == null) {
            runServer();
            gamePanel.startGame(playerName);
            if (!gameService.isGameStarted()) {
                if (server != null) {
                    server.stop();
                    server = null;
                }
            }
        } else {
            gamePanel.startGame(playerName);
        }

    }
    private void runServer() {
        new Thread(() -> {
            try {
                server = new GameServer(ConfigValues.PORT);
                server.start();
            } catch (IOException e) {
                System.out.println("Port is already in use!");
                hostCheckBox.setSelected(false);
            }
        }).start();
    }
}