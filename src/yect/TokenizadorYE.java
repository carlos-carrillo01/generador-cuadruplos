package yect;

import java.util.ArrayList;
import java.util.List;

public class TokenizadorYE {

    // las palabras que salen al separar
    private ArrayList<String> palabras = new ArrayList<String>();

    // PASO 1: separar el texto en palabras
    public void separarPalabras(String codigo) {
        palabras.clear(); //limpia la lista antes empezar
        int i = 0; // inicializa en 0 para la posicion del caracter que esta examinando

        while (i < codigo.length()) { //mientra i este dentro de los limites
            char c = codigo.charAt(i); //char representa 1 solo caracter y charAt(i) le da el caracter que esta en la posicion i

            // espacios y saltos de linea se ignoran
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                i = i + 1; //avanza al siguiente sin aceptar el espacio
                continue;
            }

            // comentario con //  se ignora hasta el final de la linea
            if (c == '/' && i + 1 < codigo.length() && codigo.charAt(i + 1) == '/') {
                // si empieza con // avanza hasta encontrar el final del texto o un salto de linea
                while (i < codigo.length() && codigo.charAt(i) != '\n') {
                    i = i + 1;
                }
                continue;  //luego de encontrar el salto de linea continua al while
            }

            // texto entre comillas, se guarda con sus comillas
            if (c == '"') {
                int inicio = i; //guarda la posicion de la comilla inicial para extraer todo enlas ""
                i = i + 1;  //avanza para encontrar la " de cierre
                //avanza hasta que encuentre otra comilla docle o llega al final del texto
                while (i < codigo.length() && codigo.charAt(i) != '"') {
                    i = i + 1;
                }
                if (i < codigo.length()) {
                    i = i + 1; // pasamos la comilla que cierra
                }
                palabras.add(codigo.substring(inicio, i)); //extrae y guarda en la lista desde el inicio hasta i
                continue;
            }

            // palabra o numero (letras, digitos, guion bajo y punto si es numero)
            if (esLetra(c) || esDigito(c) || c == '_') { //decide si el caracter inicia como nnumero o letra o el caracter _
                int inicio = i; //recuerda donde empieza para poder extraer una palabra cuando termine
                boolean empiezaConDigito = esDigito(c); // guarda verdadero o falso para que la secuencia comiece con un numero y no en caso de que sea letra
                while (i < codigo.length()) {
                    //recorre los caracteres hasta que encuentre algo que no parezca de ella
                    char d = codigo.charAt(i);
                    //si el caracter es una letra, un digito o _, lo considera parte de la misma pieza
                    if (esLetra(d) || esDigito(d) || d == '_') {
                        i = i + 1;
                    } else if (d == '.' && empiezaConDigito) {  //revisa si era un . para cosas como 3.1416 y la tome como una pieza
                        i = i + 1;
                    } else {
                        break;
                    }
                }
                palabras.add(codigo.substring(inicio, i));  //extrae y guarda en la lista desde el inicio hasta i
                continue;
            }

            // simbolos de dos caracteres: == != >= <= && ||
            if (i + 1 < codigo.length()) { //comprueba que exista el siguiente caracter
                String par = codigo.substring(i, i + 2); //extrae dos caracteres y lo guarda como text
                if (par.equals("==") || par.equals("!=") || par.equals(">=") || par.equals("<=") || par.equals("&&") || par.equals("||")) {
                    palabras.add(par); // si el par coincide lo guarda en una sola pieza
                    i = i + 2;
                    continue;
                }
            }

            // cualquier otro simbolo de un solo caracter
            palabras.add("" + c);
            i = i + 1;
        }
    }

    public ArrayList<String> getPalabras() {
        return new ArrayList<String>(palabras);
    }

    private boolean esLetra(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean esDigito(char c) {
        return c >= '0' && c <= '9';
    }
}
