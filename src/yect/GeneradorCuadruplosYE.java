package yect;

import java.util.ArrayList;
import java.util.Stack;

public class GeneradorCuadruplosYE {

    // variables de la clase

    private ArrayList<String> palabras;
    private int pos; //indice que apunta a la palabra que se esta leyendo

    // los cuadruplos (op, operando1, operando2, resultado)
    // cada uno tiene su propia lista, la posicion 0 de las 4 listas es el cuadruplo 1
    //private para que los objetos no accedan directamente a ella, solo los nmetods de la propia clase si 
    private ArrayList<String> cuadOp = new ArrayList<String>(); //columna operacion
    private ArrayList<String> cuadA = new ArrayList<String>();  //columna operando A
    private ArrayList<String> cuadB = new ArrayList<String>();  //columna operando B
    private ArrayList<String> cuadR = new ArrayList<String>();  //columna Resultado

    private int contadorTemporales = 0; //forma los nombre para los resultados tmporales t1, t2, t3, ...
    private int contadorEtiquetas = 0; //forma los nombres para los saltos Salto1, Salto2, ...

    public GeneradorCuadruplosYE(ArrayList<String> palabras) {
        this.palabras = new ArrayList<String>(palabras);
    }

    // PASO 2: convertir las palabras en cuadruplos
    public void generarCuadruplos() {
        pos = 0; //indica que palabra se esta analizando
        // se limpian las 4 listas para que no se mezclen con resultados anteriores
        cuadOp.clear();
        cuadA.clear();
        cuadB.clear();
        cuadR.clear();
        // reinicia los contadores temporales t1, t2 y etiquetas Salto1, Salto2
        contadorTemporales = 0;
        contadorEtiquetas = 0;

        if (actualEs("ye_init")) {
            agregar("inicio", "", "", "");
            pos = pos + 1; //avanza para dejar atras el inicio
        }

        bloque(); //pide que procese lo que viene enseguida

        if (actualEs("kanye_end")) {
            agregar("fin", "", "", "");
        }
    }

    // varias sentencias seguidas, hasta encontrar } o el final del programa
    private void bloque() { //privado porque el trabajo lo dirigen otros metodos al traductor
        //sigue leyendo mientras haya palabras y no se llegue al final
        while (pos < palabras.size() && !actualEs("kanye_end") && !actualEs("}")) {
            sentencia();
        }
    }

    // mira la palabra actual y decide que sentencia es
    private void sentencia() {
        String p = actual(); //palabra en posicion actual

        //si p es uno de los tipos reconocidos  llama a una declaracion
        if (esTipo(p)) {
            declaracion();
        } else if (p.equals("ye_if")) {
            sentenciaIf(); //llama al metodo if
        } else if (p.equals("ct_while")) {
            sentenciaWhile(); //procesa un ciclo while
        } else if (p.equals("ct_for")) {
            sentenciaFor(); //procesa un ciclo for
        } else if (p.equals("ye_print")) {
            sentenciaPrint(); // procesa la instruccion de impresion
        } else if (p.equals("ye_input")) {
            sentenciaInput(); // procesa la solicitud de datos
            //si la primer letra es una letra o un _ supone que puede ser una asignacion de variable
        } else if ((esLetra(p.charAt(0)) || p.charAt(0) == '_') && siguienteEs("=")) {
            
            // empieza con letra, entonces es una asignacion: x = 5;
            asignacion(buscar(";", pos));//pasa la posicion como limite  y procesa lo que hay antes de ese limite
            if (actualEs(";")) { //
                pos = pos + 1;
            }
        } else {
            pos = pos + 1; // si no reconoce algo, lo saltamos
        }
    }

    // ct_int edad = 20;     o     ct_int edad;
    private void declaracion() {
        //guarda el tipo y avanza para llegar al nombre de variable
        String tipo = actual();
        pos = pos + 1;
        //guarda el nombre de variable y avanza
        String nombre = actual();
        pos = pos + 1;

        // SIEMPRE se genera el cuadruplo de declaracion, tenga o no valor inicial
        agregar("declarar", tipo, "", nombre);

        //si lo que sigue es = avanza al primer elemento del valor inicial
        if (actualEs("=")) {
            pos = pos + 1;
            
            //busca el ; para tener el limite de la expresion y llama expresion para procesar el valor
            String valor = expresion(buscar(";", pos));
            //agrega el cuadruplo de asignacion (la inicializacion)
            agregar("=", valor, "", nombre);
        }

        if (actualEs(";")) { //si es ; solo lo consume
            pos = pos + 1;
        }
    }

    // x = x + 1   el punto y coma lo quita quien la llama
    private void asignacion(int limite) { //recibe el limite hasta donde debe leer los elementos de asignacion
        //toma la palabra actual y avanza
        String nombre = actual();
        pos = pos + 1;
        
        pos = pos + 1; // avanza otra posicion suponiendo que esta el signo =
        String valor = expresion(limite); //pide que expresion  procese desde el inicio hasta el limite
        agregar("=", valor, "", nombre); //anota el cuadruplo de asignacion: nombre = valor;
    }

    // ye_print algo;     ye_print(algo);     ye_print("Suma", suma);
    private void sentenciaPrint() {
        pos = pos + 1; //deja atras la palabra ye_print
        int finSentencia = buscar(";", pos); //posicion del ; (limite de toda la sentencia)
        int fin = finSentencia;

        // si todo va envuelto en parentesis ye_print( ... ) se quitan; si no, es parte de la expresion
        if (actualEs("(") && buscarCierre(pos + 1) == fin - 1) {
            pos = pos + 1;
            fin = fin - 1;
        }

        String valor = concatenarArgumentos(fin);

        agregar("mostrar", "", "", valor); //genera el cuadruplo de salida
        pos = finSentencia;
        if (actualEs(";")) { //consume el ; si esta ahi
            pos = pos + 1;
        }
    }

    // procesa varios argumentos separados por coma (desde pos hasta "fin") y los une con "concatenar"
    // regresa el nombre del ultimo temporal (o del unico argumento si solo hay uno)
    private String concatenarArgumentos(int fin) {
        String valor = null;
        while (true) {
            int coma = buscarComa(pos, fin);
            String arg = expresion(coma); // procesa un argumento (deja pos en la coma o en fin)
            if (valor == null) {
                valor = arg;
            } else {
                contadorTemporales = contadorTemporales + 1;
                String temporal = "t" + contadorTemporales;
                agregar("concatenar", valor, arg, temporal);
                valor = temporal;
            }
            if (coma >= fin) {
                break;
            }
            pos = coma + 1; // salta la coma
        }
        return valor;
    }

    // ye_input variable;     ye_input "Texto", variable;     ye_input("Texto", variable);

    private void sentenciaInput() {
        pos = pos + 1; //deja atras la palabra ye_input

        // ¿viene un texto antes de la variable?  ye_input "Texto", n;   o   ye_input("Texto", n);
        boolean conParentesis = actualEs("(") && pos + 1 < palabras.size() && palabras.get(pos + 1).startsWith("\"");
        if (conParentesis || actual().startsWith("\"")) {
            int finSentencia = buscar(";", pos);
            int fin = finSentencia;
            if (conParentesis) {
                pos = pos + 1; // salta el (
                fin = buscarCierre(pos); // posicion del ) que cierra
            }
            String nombre = palabras.get(fin - 1); // la variable es lo ultimo
            int finTexto = fin - 1;
            if (finTexto - 1 >= pos && palabras.get(finTexto - 1).equals(",")) {
                finTexto = finTexto - 1; // quita la coma que separa el texto de la variable
            }
            if (finTexto > pos) {
                String texto = concatenarArgumentos(finTexto);
                agregar("mostrar", "", "", texto); // primero muestra el mensaje
            }
            agregar("pedir", "", "", nombre); // y luego pide el dato
            pos = finSentencia;
        } else {
            String nombre = actual(); //toma el siguiente como nombre de la variable que recibe el dato y avanza
            pos = pos + 1;
            agregar("pedir", "", "", nombre); //agrega cuadruplo que pide un dato nombre = ye_input()
        }
        if (actualEs(";")) {
            pos = pos + 1;
        }
    }

    // ye_if (condicion) { ... } ye_else { ... }
    private void sentenciaIf() {
        pos = pos + 1; // consume ye_if
        pos = pos + 1; // y consume el (
        int cierre = buscarCierre(pos); //busca el ) y guarda su posicion
        String condicion = expresion(cierre); // //procesa lo que esta dentro de la condicion hasta el cierre
        pos = pos + 1; //  consume el )
        pos = pos + 1; // y consume el { y avanza

        String etiquetaSino = nuevaEtiqueta(); //crea el salto  L1 si resulta falsa
        agregar("si_falso", condicion, "", etiquetaSino);  // genera el cuadruplo con salto y salta a la etiquetaSino

        bloque();//procesa las sentencias dentro del bloque del if
        pos = pos + 1; // consume el }

        if (actualEs("ye_else")) {
            String etiquetaFin = nuevaEtiqueta(); //lugar que salta despues de ejecutar el bloque 
            agregar("ir_a", "", "", etiquetaFin); //salto a etiqueta fin
            agregar("inicio_else", "", "", etiquetaSino);// aqui empieza el else (a donde salta si la condicion era falsa)

            pos = pos + 1; // ye_else
            if (actualEs("ye_if")) {
                sentenciaIf(); // ye_else ye_if (...) { ... }
            } else {
                pos = pos + 1; // {
                bloque(); //ejecuta el codigo que esta dentro de ese bloque
                pos = pos + 1; // }
            }

            agregar("fin_if", "", "", etiquetaFin);  //define el lugar donde se rencuentran los dos caminos despues de ejecutar el if y el else
        } else {
            agregar("fin_if", "", "", etiquetaSino); // si la condicion fue falsa el salto llega aqui y continua despues del if
        }
    }

    // ct_while (condicion) { ... }
    private void sentenciaWhile() {
        pos = pos + 1; // ct_while
        pos = pos + 1; // (

        String etiquetaInicio = nuevaEtiqueta(); //punto al que regresara el ciclo para revisar de nuevo la condicion
        String etiquetaFin = nuevaEtiqueta(); // destino para salir del ciclo cuando la condicion sea falsa

        agregar("inicio_while", "", "", etiquetaInicio); //marca el inicio del ciclo

        int cierre = buscarCierre(pos); //busca el )
        String condicion = expresion(cierre);
        pos = pos + 1; // )
        pos = pos + 1; // {

        agregar("si_falso", condicion, "", etiquetaFin); // si la condicion es falsa el flujo salta a la etiqueta de salida
        
        
        //procesa el cuerco que esta dentro y consume el }
        bloque();
        pos = pos + 1; // }

        //agrega un salto al comienzo para volver a comprobar la condicion
        agregar("ir_a", "", "", etiquetaInicio); 
        agregar("fin_while", "", "", etiquetaFin); //
    }

    // ct_for (inicio; condicion; incremento) { ... }
    private void sentenciaFor() {
        pos = pos + 1; // ct_for
        pos = pos + 1; // (

        // la parte de inicio es una declaracion o asignacion normal
        sentencia();

        //crea la etiqueta de comienzo y la de salida, y marca el comienzo del ciclo
        String etiquetaInicio = nuevaEtiqueta();
        String etiquetaFin = nuevaEtiqueta();
        agregar("inicio_for", "", "", etiquetaInicio); 

        //procesa toda la expresion hasta que encuentre el ;
        String condicion = expresion(buscar(";", pos));
        pos = pos + 1; // el ;
        //agrega el salto de salida si la condicion es falsa
        agregar("si_falso", condicion, "", etiquetaFin); 

        // el incremento se escribe antes del cuerpo, pero se ejecuta al final
        // entonces guardamos donde esta, hacemos el cuerpo, y luego volvemos
        int inicioIncremento = pos;
        int cierre = buscarCierre(pos);

        pos = cierre + 2; // saltamos el ) y el {
        bloque();
        int posLlave = pos;

        pos = inicioIncremento;
        asignacion(cierre);

        pos = posLlave + 1; // pasamos el }
        //genera el salto al inicio para volver a evaluar a condicion  y define la etiqueta de salida
        agregar("ir_a", "", "", etiquetaInicio); 
        agregar("fin_for", "", "", etiquetaFin);
    }
    
    
    
    

    // lee una expresion como  n + a * 3  y va escribiendo cuadruplos
    // usa dos pilas, igual que la postfija:
    //   una pila para los numeros/variables y otra para los operadores
    private String expresion(int limite) {
        Stack<String> operandos = new Stack<String>(); //guarda numeros, variables y resultados temporales t1, t2, t...
        Stack<String> operadores = new Stack<String>(); //guarda operadores pendientes y parentesis 
        boolean esperaOperando = true; //sirve para distinguir el - unario (-5) del binario (a - 5)

        while (pos < limite) {  //recorre la lista palabras desde la posicion actual hasta el limite
            String p = actual();

            if (p.equals("(")) {
                operadores.push(p);
                esperaOperando = true;
            } else if (p.equals("!")) {
                operadores.push(p); // negacion logica, es unario
                esperaOperando = true;
            } else if (p.equals(")")) {
                // aplicamos operadores hasta encontrar el (
                while (!operadores.isEmpty() && !operadores.peek().equals("(")) {
                    aplicar(operadores, operandos);
                }
                if (!operadores.isEmpty()) {
                    operadores.pop(); // quitamos el (
                }
                esperaOperando = false;
            } else if (prioridad(p) > 0) {
                if (p.equals("-") && esperaOperando) {
                    operandos.push("0"); // -x se trata como 0 - x
                }
                // es un operador, primero aplicamos los que tienen mas o igual prioridad
                while (!operadores.isEmpty() && !operadores.peek().equals("(")
                        && prioridad(operadores.peek()) >= prioridad(p)) {
                    aplicar(operadores, operandos);
                }
                operadores.push(p);
                esperaOperando = true;
            } else {
                // es un numero, un texto o una variable
                operandos.push(p);
                esperaOperando = false;
            }

            pos = pos + 1;
        }

        // lo que quedo en la pila de operadores tambien se aplica
        while (!operadores.isEmpty()) {
            aplicar(operadores, operandos);
        }

        pos = limite;

        if (operandos.isEmpty()) {
            return "";
        }
        return operandos.pop();
    }

    // saca un operador y dos operandos, escribe el cuadruplo y guarda el temporal
    private void aplicar(Stack<String> operadores, Stack<String> operandos) {
        String op = operadores.pop();
        if (op.equals("!")) { // unario: solo lleva un operando
            if (operandos.isEmpty()) {
                return;
            }
            String x = operandos.pop();
            contadorTemporales = contadorTemporales + 1;
            String tmp = "t" + contadorTemporales;
            agregar("!", x, "", tmp);
            operandos.push(tmp);
            return;
        }
        if (operandos.size() < 2) {
            return;
        }
        String b = operandos.pop(); // el de la derecha sale primero
        String a = operandos.pop();

        contadorTemporales = contadorTemporales + 1;
        String temporal = "t" + contadorTemporales;

        agregar(op, a, b, temporal);
        operandos.push(temporal);
    }

    // entre mas grande el numero, mas pronto se hace esa operacion
    private int prioridad(String op) {
        if (op.equals("!")) return 7;
        if (op.equals("*") || op.equals("/") || op.equals("%")) return 6;
        if (op.equals("+") || op.equals("-")) return 5;
        if (op.equals("<") || op.equals(">") || op.equals("<=") || op.equals(">=")) return 4;
        if (op.equals("==") || op.equals("!=")) return 3;
        if (op.equals("&&")) return 2;
        if (op.equals("||")) return 1;
        return 0; // no es un operador
    }

    // mostrar los cuadruplos en pantalla
    public void mostrarCuadruplos() {
        for (int i = 0; i < cuadOp.size(); i++) {
            System.out.println((i + 1) + "  (" + cuadOp.get(i) + ", "
                    + cuadA.get(i) + ", " + cuadB.get(i) + ", "
                    + cuadR.get(i) + ")");
        }
    }

    public Object[][] obtenerFilasParaTabla() {
        Object[][] filas = new Object[cuadOp.size()][6];
        for (int i = 0; i < cuadOp.size(); i++) {
            filas[i][0] = i + 1;
            filas[i][1] = destinoVisual(i);
            filas[i][2] = cuadOp.get(i);
            filas[i][3] = cuadA.get(i);
            filas[i][4] = cuadB.get(i);
            filas[i][5] = cuadR.get(i);
        }
        return filas;
    }

    private String destinoVisual(int indice) {
        String op = cuadOp.get(indice);
        String destino = cuadR.get(indice);

        if (op.equals("si_falso") || op.equals("ir_a")) {
            int fila = numeroFilaDestino(destino);
            return fila >= 0 ? destino + " → " + (fila + 1) : destino;
        }
        if (op.equals("inicio_for") || op.equals("fin_for")
                || op.equals("inicio_while") || op.equals("fin_while")
                || op.equals("inicio_else") || op.equals("fin_if")) {
            return destino;
        }
        return "";
    }

    private int numeroFilaDestino(String etiqueta) {
        for (int i = 0; i < cuadOp.size(); i++) {
            if (cuadR.get(i).equals(etiqueta)
                    && (cuadOp.get(i).equals("inicio_for")
                    || cuadOp.get(i).equals("fin_for")
                    || cuadOp.get(i).equals("inicio_while")
                    || cuadOp.get(i).equals("fin_while")
                    || cuadOp.get(i).equals("inicio_else")
                    || cuadOp.get(i).equals("fin_if"))) {
                return i;
            }
        }
        return -1;
    }

    // funciones 
    
    private void agregar(String op, String a, String b, String r) {
        cuadOp.add(op);
        cuadA.add(a);
        cuadB.add(b);
        cuadR.add(r);
    }

    //aumenta el contador y devuelve la etiqueta Salto1, Salto2,...
    private String nuevaEtiqueta() {
        contadorEtiquetas = contadorEtiquetas + 1;
        return "Salto" + contadorEtiquetas;
    }

    // la palabra en la posicion actual (o "" si ya se acabaron)
    //devuelve la palabra en la pos, si ya no hay palabra en esa posicion devuelve una cadena vacia
    private String actual() {
        if (pos < palabras.size()) {
            return palabras.get(pos);
        }
        return "";
    }

    //compara la palabra actal con un texto pj actualEs("ye_if") pregunta si el elemento actual es ye_if
    private boolean actualEs(String texto) {
        return actual().equals(texto);
    }

    private boolean esTipo(String p) {
        return p.equals("ct_int") || p.equals("ct_float") || p.equals("ct_string") || p.equals("ct_bool");
    }

    private boolean esLetra(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    // busca desde "desde" la siguiente palabra igual a "texto"
    private int buscar(String texto, int desde) {
        int i = desde;
        while (i < palabras.size() && !palabras.get(i).equals(texto)) {
            i = i + 1;
        }
        return i;
    }

    // la palabra que sigue a la actual es "texto"?
    private boolean siguienteEs(String texto) {
        return pos + 1 < palabras.size() && palabras.get(pos + 1).equals(texto);
    }

    // busca la primera coma que no este dentro de parentesis, entre "desde" y "hasta" (si no hay regresa hasta)
    private int buscarComa(int desde, int hasta) {
        int profundidad = 0;
        for (int i = desde; i < hasta && i < palabras.size(); i++) {
            String p = palabras.get(i);
            if (p.equals("(")) profundidad = profundidad + 1;
            if (p.equals(")")) profundidad = profundidad - 1;
            if (p.equals(",") && profundidad == 0) {
                return i;
            }
        }
        return hasta;
    }

    // "desde" esta justo despues de un (  regresa donde esta el ) que lo cierra
    private int buscarCierre(int desde) {
        int profundidad = 1;
        int i = desde;
        while (i < palabras.size()) {
            if (palabras.get(i).equals("(")) profundidad = profundidad + 1;
            if (palabras.get(i).equals(")")) profundidad = profundidad - 1;
            if (profundidad == 0) {
                return i;
            }
            i = i + 1;
        }
        return i;
    }
}
