package abans;

public class Client {
    public static void main(String[] args){
        CalculadoraEntrada calculadora = new CalculadoraEntrada();
        String temps = "";
        temps = "mati";
        System.out.println(calculadora.mostrarPerOnEntrar(temps));
        temps = "migdia";
        System.out.println(calculadora.mostrarPerOnEntrar(temps));
        temps = "tarda";
        System.out.println(calculadora.mostrarPerOnEntrar(temps));
        temps = "vespre";
        System.out.println(calculadora.mostrarPerOnEntrar(temps));
        temps = "nit";
        System.out.println(calculadora.mostrarPerOnEntrar(temps));
    }
}
