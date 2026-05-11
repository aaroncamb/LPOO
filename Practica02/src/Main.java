import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Práctica 2 — Programa principal.
 *
 * Demuestra la creación y uso de los tres constructores de Cliente,
 * la gestión a través de GestorClientes, y operaciones del dominio.
 */
public class Main {

    public static void main(String[] args) {
        GestorClientes gestor = new GestorClientes();

        // 1) Constructor vacío + asignación manual.
        //    Simula el caso de un formulario de alta donde se llenan los
        //    campos uno por uno mientras el usuario teclea.
        Cliente c1 = new Cliente();
        c1.id = 1001;
        c1.nombreCompleto = "Ana Gabriela Pérez Soto";
        c1.email = "ana.perez@correo.mx";
        c1.fechaRegistro = LocalDate.of(2024, 11, 5);
        c1.pesoKg = 62.0;

        // 2) Constructor mínimo: alta rápida en mostrador.
        //    El peso queda en 0 hasta que el cliente suba a la báscula.
        Cliente c2 = new Cliente(1002, "Bruno Hernández Lara", "bruno.h@correo.mx");

        // 3) Constructor completo: cliente importado desde un archivo viejo.
        Cliente c3 = new Cliente(1003, "Carolina Méndez Gómez",
                "carolina.m@correo.mx", LocalDate.of(2023, 3, 18), 70.5);

        // 4) Otro completo, para tener un veterano (>12 meses) en los datos.
        Cliente c4 = new Cliente(1004, "David Ortega Ruiz",
                "david.ortega@correo.mx", LocalDate.of(2022, 6, 1), 88.2);

        // 5) Mínimo, para mostrar a un cliente recién dado de alta.
        Cliente c5 = new Cliente(1005, "Elena Salinas Díaz", "elena.s@correo.mx");

        // Agregar todos al gestor.
        gestor.agregar(c1);
        gestor.agregar(c2);
        gestor.agregar(c3);
        gestor.agregar(c4);
        gestor.agregar(c5);

        // Demostrar la detección de duplicados.
        System.out.println("Intento agregar un cliente con id repetido (1003):");
        boolean ok = gestor.agregar(new Cliente(1003, "Otro", "x@x.mx"));
        System.out.println("  resultado = " + ok + "  (esperado: false)\n");

        // Mostrar a todos.
        gestor.mostrarTodos();
        System.out.println();

        // Búsqueda por id.
        System.out.println("Búsqueda por id 1004:");
        Optional<Cliente> encontrado = gestor.buscarPorId(1004);
        encontrado.ifPresentOrElse(
                c -> System.out.println("  " + c),
                () -> System.out.println("  no encontrado"));

        System.out.println("\nBúsqueda por id 9999 (inexistente):");
        gestor.buscarPorId(9999).ifPresentOrElse(
                c -> System.out.println("  " + c),
                () -> System.out.println("  no encontrado"));

        // Búsqueda parcial por nombre.
        System.out.println("\nBúsqueda por nombre que contenga 'me':");
        List<Cliente> coincidencias = gestor.buscarPorNombre("me");
        for (Cliente c : coincidencias) {
            System.out.println("  " + c);
        }

        // Demostrar métodos del dominio.
        System.out.println("\n--- Operaciones de dominio ---");
        System.out.println("Saludo a c2:        Hola, " + c2.primerNombre() + "!");
        System.out.println("¿c4 es veterano?    " + c4.esVeterano());
        System.out.println("¿c5 es veterano?    " + c5.esVeterano());

        System.out.printf("Peso de c1 antes:   %.1f kg%n", c1.pesoKg);
        c1.actualizarPeso(60.5);
        System.out.printf("Peso de c1 después: %.1f kg%n", c1.pesoKg);

        double pesoFinal = c4.registrarCambioPeso(-2.3);
        System.out.printf("c4 baja 2.3kg, ahora pesa %.1f kg%n", pesoFinal);

        // Demostrar equals (igualdad por id, no por referencia).
        System.out.println("\n--- equals ---");
        Cliente otroConMismoId = new Cliente(1001, "Nombre distinto", "otro@x.mx");
        System.out.println("c1 == otroConMismoId: " + (c1 == otroConMismoId));
        System.out.println("c1.equals(otroConMismoId): " + c1.equals(otroConMismoId));
        System.out.println("(equals es true porque comparamos por id de negocio)");
    }
}
