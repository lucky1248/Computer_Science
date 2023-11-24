package despres;

import abans.CalculadoraEntrada;

public class Client2 {
    public static void main(String[] args) {
        despres.CalculadoraEntrada calculadora = new despres.CalculadoraEntrada();
        CalculadoraEntradaStrategy strategy = null;
        strategy = new MatiStrategy();
        calculadora.setStrategy(strategy);
        System.out.println(calculadora.mostrarPerOnEntrar());
        strategy = new MigdiaStrategy();
        calculadora.setStrategy(strategy);
        System.out.println(calculadora.mostrarPerOnEntrar());
        strategy = new TardaStrategy();
        calculadora.setStrategy(strategy);
        System.out.println(calculadora.mostrarPerOnEntrar());
        strategy = new VespreStrategy();
        calculadora.setStrategy(strategy);
        System.out.println(calculadora.mostrarPerOnEntrar());
        strategy = new NitStrategy();
        calculadora.setStrategy(strategy);
        System.out.println(calculadora.mostrarPerOnEntrar());
    }
}
