package despres;

public class TardaStrategy implements CalculadoraEntradaStrategy {
    @Override
    public String mostrarPerOnEntrar() {
        return "Entra per la Gran Via";
    }
}