# campuslab-ms-notify

Microservicio Spring Boot encargado de consumir mensajes RabbitMQ para notificaciones, preparación de tickets y generación de vouchers dentro de CampusLab.

## Tecnologías

- Java 17
- Spring Boot 3.2.3
- Spring AMQP
- RabbitMQ
- Maven
- Docker

## Puerto

El microservicio se ejecuta en:

```text
8083
```

## Responsabilidad

`campuslab-ms-notify` consume mensajes publicados por `campuslab-ms-bookings` mediante RabbitMQ.

Flujo general:

```text
campuslab-ms-bookings
        |
        | publica eventos
        v
     RabbitMQ
        |
        | entrega mensajes
        v
campuslab-ms-notify
```

## Colas consumidas

| Cola | Función |
|---|---|
| `q.cmd.email` | Procesamiento de notificaciones por email |
| `q.cmd.prep` | Preparación de tickets |
| `q.cmd.voucher` | Procesamiento de vouchers |

## Dead Letter Queues

| Cola principal | DLQ |
|---|---|
| `q.cmd.email` | `q.cmd.email.dlq` |
| `q.cmd.prep` | `q.cmd.prep.dlq` |
| `q.cmd.voucher` | `q.cmd.voucher.dlq` |

Las colas principales utilizan el exchange de mensajes fallidos:

```text
cmd.dead.dlx
```

Cada cola principal configura además una routing key específica para su respectiva DLQ.

## Configuración centralizada

Los nombres utilizados por RabbitMQ se encuentran centralizados en:

```text
src/main/resources/application.yml
```

Ejemplo:

```yaml
campuslab:
  rabbitmq:
    exchanges:
      dead: cmd.dead.dlx

    queues:
      email: q.cmd.email
      prep: q.cmd.prep
      voucher: q.cmd.voucher

      email-dlq: q.cmd.email.dlq
      prep-dlq: q.cmd.prep.dlq
      voucher-dlq: q.cmd.voucher.dlq
```

Esto evita dejar nombres de colas o exchanges escritos directamente en la lógica de negocio.

## Consumidores

Los consumidores se encuentran en:

```text
src/main/java/cl/duoc/campuslab/notify/messaging/consumer
```

Clases implementadas:

- `EmailConsumer`
- `PrepConsumer`
- `VoucherConsumer`

Cada consumidor utiliza `@RabbitListener` para escuchar su cola correspondiente.

Ejemplo:

```java
@RabbitListener(
        queues = "${campuslab.rabbitmq.queues.email}",
        ackMode = "MANUAL"
)
```

## Confirmación de mensajes

Los consumidores utilizan confirmación manual de mensajes.

Cuando el procesamiento es correcto:

```java
channel.basicAck(deliveryTag, false);
```

Cuando ocurre un error:

```java
channel.basicNack(
        deliveryTag,
        false,
        false
);
```

El último parámetro en `false` evita reencolar indefinidamente el mensaje y permite que RabbitMQ lo derive a la DLQ configurada.

## Manejo de errores

Cada consumidor encapsula el procesamiento dentro de un bloque `try/catch`.

En caso de éxito:

```text
ACK enviado
```

En caso de error:

```text
NACK enviado
Mensaje derivado a DLQ
```

Esto permite controlar explícitamente el flujo de mensajes fallidos.

## Envelope de mensajes

Los eventos consumidos utilizan el DTO:

```text
BookingEventMessage
```

El mensaje contiene los siguientes campos:

| Campo | Descripción |
|---|---|
| `bookingId` | Identificador de la reserva |
| `status` | Estado actual de la reserva |
| `type` | Tipo de evento |
| `message` | Descripción del evento |
| `eventId` | Identificador único del evento |
| `traceId` | Identificador de trazabilidad |
| `correlationId` | Relación entre mensajes de una misma reserva |
| `timestamp` | Fecha y hora de generación del evento |

Esto permite mantener trazabilidad entre el productor y los consumidores.

## Ejemplo de procesamiento exitoso

Ejemplo de log generado al consumir una notificación:

```text
EMAIL recibido:
Reserva ID: 1
Estado: SOLICITADA
Tipo: BOOKING_CREATED
Mensaje: Reserva creada correctamente
Event ID: ...
Trace ID: ...
Correlation ID: booking-1
Timestamp: ...
Email procesado correctamente. ACK enviado.
```

## Evidencia de manejo de errores y DLQ

Se realizó una prueba controlada provocando un error durante el procesamiento de un voucher.

El consumidor registró:

```text
Error procesando voucher: Fallo controlado para probar DLQ
NACK enviado. Mensaje derivado a DLQ.
```

Posteriormente se verificó RabbitMQ con:

```powershell
docker exec campuslab-rabbitmq rabbitmqctl list_queues name messages_ready messages_unacknowledged
```

Resultado relevante:

```text
q.cmd.voucher.dlq    1    0
```

Esto evidencia que el mensaje fallido fue derivado correctamente hacia la Dead Letter Queue.

Flujo comprobado:

```text
Mensaje
   |
   v
VoucherConsumer
   |
   | error
   v
NACK
   |
   v
cmd.dead.dlx
   |
   v
q.cmd.voucher.dlq
```

El fallo controlado utilizado para la prueba fue eliminado posteriormente del código final.

## Variables de entorno

El microservicio utiliza variables de entorno para conectarse a RabbitMQ.

| Variable | Descripción | Valor local |
|---|---|---|
| `RABBITMQ_HOST` | Host del servidor RabbitMQ | `rabbitmq` |
| `RABBITMQ_PORT` | Puerto de RabbitMQ | `5672` |
| `RABBITMQ_USERNAME` | Usuario de conexión | `campuslab` |
| `RABBITMQ_PASSWORD` | Contraseña de conexión | `campuslab123` |

Estas variables se inyectan desde `docker-compose.yml`, permitiendo que el contenedor se conecte al broker RabbitMQ dentro de la red Docker.

## Compilación

Para compilar el proyecto:

```powershell
.\mvnw.cmd clean package
```

Resultado esperado:

```text
BUILD SUCCESS
```

## Docker

Para construir la imagen:

```powershell
docker build -t lukmezac/campuslab-ms-notify:latest .
```

El contenedor expone el puerto:

```text
8083
```

## Integración con CampusLab

El flujo implementado es:

```text
campuslab-ms-bookings
        |
        | publica eventos
        v
     RabbitMQ
        |
        +----------------------+
        |                      |
        v                      v
 q.cmd.email              q.cmd.prep
        |                      |
        v                      v
 EmailConsumer            PrepConsumer

        |
        v
 q.cmd.voucher
        |
        v
 VoucherConsumer
```

En caso de fallo:

```text
Consumer
   |
   | NACK
   v
cmd.dead.dlx
   |
   v
DLQ correspondiente
```

## Estado

El microservicio se encuentra implementado y probado localmente con:

- consumo mediante RabbitMQ
- consumidores con `@RabbitListener`
- ACK manual
- NACK manual
- manejo explícito de errores
- envío de mensajes fallidos a DLQ
- trazabilidad mediante envelope de eventos
- configuración centralizada
- ejecución mediante Docker
