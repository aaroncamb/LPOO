# Reflexión — Práctica 3

## 1. ¿Por qué marcamos atributos como `private`? ¿Qué riesgo evitamos?

Los marcamos `private` para que ningún código fuera de la clase pueda
leerlos ni modificarlos directamente. El acceso obligado pasa por métodos
(getters/setters) que la propia clase controla.

El riesgo concreto que evitamos es que el objeto **entre en un estado
inválido sin que la clase se entere**. En mi P2, los atributos eran
package-private, así que cualquier código en el mismo paquete podía hacer
`cliente.pesoKg = -500;` y dejar al cliente con un peso negativo, lo cual
no representa nada en el mundo real. Después, cuando otra parte del programa
intentara calcular el IMC con ese peso, el resultado sería absurdo, y ahora
sí explotaría algo, pero lejos de donde estuvo el bug original.

Con `private` la asignación inválida se rechaza en el momento exacto en que
ocurre. El setter lanza `IllegalArgumentException` con un mensaje
descriptivo, y el código que intentó hacer mal queda señalado en la traza
de la pila. Eso ahorra horas de depuración.

Otro riesgo asociado es el **acoplamiento**: si los atributos fueran
públicos, cualquier código que lee `cliente.pesoKg` queda atado a que la
clase represente el peso exactamente como un `double` en kilogramos.
El día que yo decida guardarlo en gramos como `int`, o tener un objeto
`Peso` con unidad y valor, todo ese código externo se rompe. Con getters
puedo cambiar la representación interna sin que el exterior se entere,
mientras `getPesoKg()` siga devolviendo un `double` con kilogramos.

## 2. ¿Cuál es la diferencia entre `private`, `protected` y `public`? Ilustra con un ejemplo de tu código.

Los tres modificadores definen quién puede ver un miembro de la clase:

- **`public`**: lo ve todo el mundo. Cualquier código del proyecto puede
  acceder. En mi código son `public` los constructores, los getters/setters
  y los métodos de dominio como `calcularIMC()`, `estaVigente()`,
  `renovar(int)`. Son la interfaz oficial: cualquiera puede llamarlos.

- **`private`**: lo ve solo la propia clase. Nadie más, ni siquiera una
  subclase. En `Cliente` son `private` todos los atributos (`id`,
  `nombreCompleto`, `email`, `fechaRegistro`, `pesoKg`, `alturaCm`,
  `membresia`). Externamente solo se puede acceder por los métodos públicos
  que yo controlo.

- **`protected`**: lo ve la propia clase, las subclases (aunque estén en
  otro paquete) y las clases del mismo paquete. En `Membresia` los atributos
  son `protected` porque sé que en P4 voy a crear `MembresiaBasica`,
  `MembresiaPremium` y `MembresiaVIP` que heredan de `Membresia`. Esas
  subclases necesitan acceso directo a `precioMensual`, `tipo` y las fechas
  para sobrescribir el comportamiento. Si fueran `private` tendrían que
  llegar por getter cada vez, lo cual funciona pero es verboso.

Ejemplo concreto del contraste:

```java
public class Cliente {
    private double pesoKg;          // solo Cliente puede tocarlo
    public double getPesoKg() {...} // todo el mundo puede pedirlo
}

public class Membresia {
    protected double precioMensual; // Membresia y sus futuras hijas
}
```

En P4 voy a poder escribir dentro de `MembresiaVIP`:

```java
public class MembresiaVIP extends Membresia {
    @Override
    public double calcularPrecioMensual() {
        return precioMensual * 1.10; // acceso directo, sin getter
    }
}
```

Si `precioMensual` hubiera sido `private`, esa línea no compila.

## 3. ¿Qué validación incluiste en un setter? ¿Qué pasa si el valor recibido es inválido?

Una de las que más me gusta es la de `setPesoKg`:

```java
public void setPesoKg(double pesoKg) {
    if (pesoKg == 0.0) {
        this.pesoKg = 0.0;     // caso especial: aun no se ha pesado
        return;
    }
    if (pesoKg < 30.0 || pesoKg > 300.0) {
        throw new IllegalArgumentException(
            "El peso debe estar entre 30 y 300 kg (o ser 0 si aun no se pesa). "
            + "Recibido: " + pesoKg);
    }
    this.pesoKg = pesoKg;
}
```

Si el valor recibido está fuera del rango permitido y no es 0, el método
**lanza una excepción `IllegalArgumentException`** con un mensaje que
incluye el valor exacto que se recibió. El atributo `pesoKg` del objeto
**no se modifica**: queda en el estado que tenía antes de la llamada. Esto
es importante porque garantiza que el objeto nunca quede con un valor
inválido, ni siquiera un instante.

Quien llamó al setter recibe la excepción y tiene que decidir qué hacer:

- En `Main.java`, los intentos están envueltos en `try/catch`, así que el
  programa imprime el mensaje y sigue funcionando.
- En `ClienteTest.java`, las pruebas usan esa excepción para confirmar que
  la validación funciona (atrapan el `IllegalArgumentException` y cuentan
  la prueba como pasada; si no se hubiera lanzado, contarían como fallada).

Elegí `IllegalArgumentException` en lugar de devolver `false` por dos
razones que discutí con la IA. Primero, devolver `false` deja al llamador
con la responsabilidad de revisar el retorno; si se olvida, el programa
parece haber asignado el peso cuando en realidad no lo hizo, lo cual es
peor que un error ruidoso. Segundo, una excepción carga el mensaje
descriptivo con el valor exacto, lo cual es oro para depuración: en los
logs voy a ver "Recibido: -500.0" en vez de un genérico "validación falló".

`IllegalArgumentException` es una `RuntimeException`, así que no estoy
obligando al llamador a hacer `throws` ni a envolverla en `try/catch`. Es
una buena señal de "este error proviene de un bug de quien me llamó, no
de una condición del mundo externo". Cuando en P7 toque crear excepciones
propias (`PagoRechazadoException` y similares), van a heredar de
`RuntimeException` por la misma razón cuando representen errores del
programador, y de `Exception` cuando representen condiciones externas
recuperables.
