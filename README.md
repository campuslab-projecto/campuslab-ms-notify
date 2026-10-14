# \# campuslab-ms-notify

# 

# Microservicio Spring Boot encargado de consumir mensajes RabbitMQ para notificaciones, preparación de tickets y generación de vouchers dentro de CampusLab.

# 

# \## Tecnologías

# 

# \- Java 17

# \- Spring Boot 3.2.3

# \- Spring AMQP

# \- RabbitMQ

# \- Maven

# \- Docker

# 

# \## Puerto

# 

# ```text

# 8083

# ```

# 

# \## Responsabilidad

# 

# Este microservicio consume mensajes publicados por `campuslab-ms-bookings` mediante RabbitMQ.

# 

# Flujo general:

# 

# ```text

# campuslab-ms-bookings

# &#x20;       |

# &#x20;       | publica eventos

# &#x20;       v

# &#x20;    RabbitMQ

# &#x20;       |

# &#x20;       | entrega mensajes

# &#x20;       v

# campuslab-ms-notify

# ```

# 

# \## Colas consumidas

# 

# | Cola | Función |

# |---|---|

# | `q.cmd.email` | Procesamiento de notificaciones por email |

# | `q.cmd.prep` | Preparación de tickets |

# | `q.cmd.voucher` | Procesamiento de vouchers |

# 

# \## Dead Letter Queues

# 

# | Cola principal | DLQ |

# |---|---|

# | `q.cmd.email` | `q.cmd.email.dlq` |

# | `q.cmd.prep` | `q.cmd.prep.dlq` |

# | `q.cmd.voucher` | `q.cmd.voucher.dlq` |

# 

# Las colas principales utilizan:

# 

# ```text

# x-dead-letter-exchange = cmd.dead.dlx

# ```

# 

# y una routing key específica para cada DLQ.

# 

# \## Configuración centralizada

# 

# Los nombres de exchanges y colas se encuentran centralizados en:

# 

# ```text

# src/main/resources/application.yml

# ```

# 

# Ejemplo:

# 

# ```yaml

# campuslab:

# &#x20; rabbitmq:

# &#x20;   exchanges:

# &#x20;     dead: cmd.dead.dlx

# 

# &#x20;   queues:

# &#x20;     email: q.cmd.email

# &#x20;     prep: q.cmd.prep

# &#x20;     voucher: q.cmd.voucher

# &#x20;     email-dlq: q.cmd.email.dlq

# &#x20;     prep-dlq: q.cmd.prep.dlq

# &#x20;     voucher-dlq: q.cmd.voucher.dlq

# ```

# 

# \## Consumidores

# 

# Los consumidores se encuentran en:

# 

# ```text

# src/main/java/cl/duoc/campuslab/notify/messaging/consumer

# ```

# 

# Clases:

# 

# ```text

# EmailConsumer

# PrepConsumer

# VoucherConsumer

# ```

# 

# Todos utilizan:

# 

# ```java

# @RabbitListener

# ```

# 

# con modo de confirmación manual:

# 

# ```text

# ackMode = MANUAL

# ```

# 

# \## ACK y NACK

# 

# Cuando un mensaje es procesado correctamente se ejecuta:

# 

# ```java

# channel.basicAck(deliveryTag, false);

# ```

# 

# Cuando ocurre un error se ejecuta:

# 

# ```java

# channel.basicNack(

# &#x20;       deliveryTag,

# &#x20;       false,

# &#x20;       false

# );

# ```

# 

# El último parámetro en `false` evita reencolar indefinidamente el mensaje y permite su derivación a la DLQ configurada.

# 

# \## Envelope de mensajes

# 

# Los eventos utilizan el DTO:

# 

# ```text

# BookingEventMessage

# ```

# 

# con los siguientes campos:

# 

# ```text

# bookingId

# status

# type

# message

# eventId

# traceId

# correlationId

# timestamp

# ```

# 

# Esto permite mantener información de trazabilidad entre productor y consumidor.

# 

# \## Ejemplo de procesamiento exitoso

# 

# ```text

# EMAIL recibido:

# Reserva ID: 1

# Estado: SOLICITADA

# Tipo: BOOKING\_CREATED

# Event ID: ...

# Trace ID: ...

# Correlation ID: booking-1

# Email procesado correctamente. ACK enviado.

# ```

# 

# \## Evidencia de manejo de errores y DLQ

# 

# Se realizó una prueba controlada provocando un error en el procesamiento de un voucher.

# 

# Resultado registrado en logs:

# 

# ```text

# Error procesando voucher: Fallo controlado para probar DLQ

# NACK enviado. Mensaje derivado a DLQ.

# ```

# 

# Posteriormente se verificó RabbitMQ:

# 

# ```text

# q.cmd.voucher.dlq    messages\_ready=1    messages\_unacknowledged=0

# ```

# 

# Esto demuestra el flujo:

# 

# ```text

# Mensaje

# &#x20;  |

# &#x20;  v

# VoucherConsumer

# &#x20;  |

# &#x20;  | error

# &#x20;  v

# NACK

# &#x20;  |

# &#x20;  v

# cmd.dead.dlx

# &#x20;  |

# &#x20;  v

# q.cmd.voucher.dlq

# ```

# 

# El fallo utilizado para la prueba fue eliminado posteriormente del código final.

# 

# \## Compilación

# 

# ```powershell

# .\\mvnw.cmd clean package

# ```

# 

# Resultado esperado:

# 

# ```text

# BUILD SUCCESS

# ```

# 

# \## Docker

# 

# Construcción de imagen:

# 

# ```powershell

# docker build -t lukmezac/campuslab-ms-notify:latest .

# ```

# 

# El servicio se ejecuta en el puerto:

# 

# ```text

# 8083

# ```

# 

# \## Variables de entorno

# 

# ```text

# RABBITMQ\_HOST

# RABBITMQ\_PORT

# RABBITMQ\_USERNAME

# RABBITMQ\_PASSWORD

# ```

# 

# Valores locales utilizados:

# 

# ```text

# RABBITMQ\_HOST=rabbitmq

# RABBITMQ\_PORT=5672

# RABBITMQ\_USERNAME=campuslab

# RABBITMQ\_PASSWORD=campuslab123

# ```

# 

# \## Estado

# 

# Implementado y probado localmente con:

# 

# \- consumo RabbitMQ

# \- `@RabbitListener`

# \- ACK manual

# \- NACK manual

# \- envío a DLQ

# \- trazabilidad mediante envelope

# \- configuración centralizada

# \- ejecución mediante Docker

