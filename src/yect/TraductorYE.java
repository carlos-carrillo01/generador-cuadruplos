package yect;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.awt.GraphicsEnvironment;
import javax.swing.SwingUtilities;

public class TraductorYE {

    public static void main(String[] args) throws IOException {
        Path ruta = Path.of(args.length > 0 ? args[0] : "/home/stravis/Downloads/pruebas/13_factorial_texto.txt");
        String codigo = Files.readString(ruta, StandardCharsets.UTF_8);

        TokenizadorYE tokenizador = new TokenizadorYE();

        System.out.println("---- codigo ----");
        System.out.println(codigo);

        tokenizador.separarPalabras(codigo);
        ArrayList<String> palabras = tokenizador.getPalabras();
        System.out.println("\n---- paso 1: palabras ----");
        System.out.println(palabras);

        GeneradorCuadruplosYE generador = new GeneradorCuadruplosYE(palabras);
        generador.generarCuadruplos();
        System.out.println("\n---- paso 2: cuadruplos ----");
        generador.mostrarCuadruplos();

        Object[][] filas = generador.obtenerFilasParaTabla();
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("No hay pantalla gráfica disponible; los cuádruplos se muestran solo en la terminal.");
        } else {
            SwingUtilities.invokeLater(() -> {
                VentanaCuadruplosYE ventana = new VentanaCuadruplosYE(filas);
                ventana.setVisible(true);
            });
        }
    }
}
