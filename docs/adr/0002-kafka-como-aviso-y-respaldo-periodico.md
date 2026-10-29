# ADR 0002: Kafka como aviso y sincronización periódica de respaldo

Fecha: 2026-10-29

## Contexto

La cátedra publica cada versión nueva del catálogo en Redis y avisa por Kafka con un
evento `CatalogUpdated`. Kafka entrega "al menos una vez": un aviso puede llegar
repetido, llegar tarde o, si la sincronización que dispara falla, no volver a leerse.

El enunciado pide que la versión local solo avance cuando la unidad de trabajo terminó
bien, que repetir una notificación no cambie el resultado, y que el servicio informe
el estado y los errores de sincronización.

## Decisión

- **Kafka solo dispara.** El listener no usa el `newVersion` del evento: llama a la
  misma sincronización que el arranque, que compara la versión local con la de Redis.
  Un aviso viejo o desordenado no puede hacer retroceder la copia.
- **Una transacción por versión.** Si falla la 7, la copia queda bien en la 6 y el
  próximo intento sigue desde ahí.
- **Dos niveles de idempotencia.** Los `eventId` procesados se guardan en
  `processed_catalog_event`; además, aplicar una versión que ya está aplicada no hace nada.
- **Respaldo periódico.** Cada 5 minutos se vuelve a sincronizar. Es la forma de
  recuperarse de un aviso perdido sin depender del siguiente.
- **Un decorador registra el resultado.** `RecordingCatalogSync` envuelve el caso de uso,
  guarda cada intento en `catalog_sync_state` (en una transacción propia) y es
  `synchronized`, así que dos sincronizaciones nunca corren a la vez.

## Consecuencias

- Las tres entradas (arranque, Kafka y tarea periódica) usan el mismo caso de uso, y el
  caso de uso sigue sin depender de Spring.
- Un aviso perdido se recupera en, como mucho, el intervalo configurado.
- Con Kafka caído el servicio sigue sincronizando, solo que con hasta 5 minutos de atraso.
- `synchronized` solo protege dentro de una instancia. Si hubiera varias instancias del
  catálogo, haría falta un lock compartido (por ejemplo en Redis o en la base).

## Alternativas descartadas

- **Reintentar el mensaje de Kafka hasta que funcione:** si la cátedra está caída, el
  consumidor queda trabado en ese mensaje. Con la tarea periódica el aviso no hace falta.
- **Aplicar directamente la versión que trae el evento:** depende de que los mensajes
  lleguen en orden y completos, que es justo lo que Kafka no garantiza.
- **Guardar el estado solo en memoria:** se pierde al reiniciar, que es cuando más se
  necesita saber qué pasó.
