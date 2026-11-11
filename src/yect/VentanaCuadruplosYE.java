package yect;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class VentanaCuadruplosYE extends JFrame {

    private static final String[] COLUMNAS = {
        "N.º", "Destino", "Operación", "Operando 1", "Operando 2", "Resultado"
    };

    private int filaDestinoResaltada = -1;

    public VentanaCuadruplosYE(Object[][] filas) {
        setTitle("Cuádruplos YE");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        DefaultTableModel modelo = new DefaultTableModel(filas, COLUMNAS) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        JTable tabla = new JTable(modelo);
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(26);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                java.awt.Component componente = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                int filaModelo = table.convertRowIndexToModel(row);
                if (!isSelected && filaModelo == filaDestinoResaltada) {
                    componente.setBackground(new Color(255, 244, 190));
                } else if (!isSelected) {
                    componente.setBackground(Color.WHITE);
                }
                return componente;
            }
        };
        tabla.setDefaultRenderer(Object.class, renderer);
        tabla.setDefaultRenderer(Number.class, renderer);

        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                int filaVista = tabla.rowAtPoint(evento.getPoint());
                if (filaVista < 0) {
                    return;
                }
                int fila = tabla.convertRowIndexToModel(filaVista);
                String operacion = String.valueOf(modelo.getValueAt(fila, 2));
                String destino = String.valueOf(modelo.getValueAt(fila, 5));

                if (operacion.equals("si_falso") || operacion.equals("ir_a")) {
                    filaDestinoResaltada = buscarFilaEtiqueta(modelo, destino);
                    if (filaDestinoResaltada >= 0) {
                        int destinoVista = tabla.convertRowIndexToView(filaDestinoResaltada);
                        tabla.scrollRectToVisible(tabla.getCellRect(destinoVista, 0, true));
                    }
                } else {
                    filaDestinoResaltada = -1;
                }
                tabla.repaint();
            }
        });

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        tabla.getColumnModel().getColumn(0).setPreferredWidth(45);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(125);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(130);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(130);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(130);

        setPreferredSize(new Dimension(850, 460));
        pack();
        setLocationRelativeTo(null);
    }

    private int buscarFilaEtiqueta(DefaultTableModel modelo, String etiqueta) {
        Map<String, Integer> destinos = new HashMap<String, Integer>();
        for (int i = 0; i < modelo.getRowCount(); i++) {
            String operacion = String.valueOf(modelo.getValueAt(i, 2));
            if (operacion.equals("inicio_for") || operacion.equals("fin_for")
                    || operacion.equals("inicio_while") || operacion.equals("fin_while")
                    || operacion.equals("inicio_else") || operacion.equals("fin_if")) {
                destinos.put(String.valueOf(modelo.getValueAt(i, 5)), i);
            }
        }
        Integer fila = destinos.get(etiqueta);
        return fila == null ? -1 : fila;
    }
}
