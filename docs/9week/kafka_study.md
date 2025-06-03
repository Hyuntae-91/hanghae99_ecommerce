# Kafka 의 구성요소

## Producer (생성자)
- Kafka 에 Message 를 Broker 에 **Publish** 하는 어플리케이션
- 메시지를 특정 Topic에 전송

## Consumer (소비자)
- Kafka 의 Broker 에서 Message 를 얽어오는 클라이언트 어플리케이션
- Message 를 읽을때 Offset 을 사용
    - Offset 을 사용하여 Message 를 어디까지 읽었는지 기록 (Current-offset 이용)
    - Offset 을 사용하여 Message 중복처리를 회피할 수 있음
    - Offset 은 Consumer Group 별로 관리됨. Group 이 다르면, 메세지를 중복해서 읽을 수 있음
        - 하나의 메세지로 여러 Service 를 Trigger 시키고 싶을 경우, Group 을 별도로 만들어서 처리
        - 하나의 Group 안에서는 같은 Offset 을 사용하므로, Message 를 중복으로 처리하지 않음

## Broker
- Producer 의 메세지를 받아서 offset 을 지정하고 Disk 에 영속화 저장
- Consumer 가 Read 요청을 하면, Disk 의 메세지를 전달
- 즉, Producer와 Consumer 간의 통신을 중개하는 서버
- 여러개의 Broker가 클러스터를 구성하여 확장성과 내결함성을 확보

## Topic & Partition
- 메시지를 분류하는 기준
- N 개의 Partition 으로 구성됨
    - Topic 을 **물리적**으로 구분함
    - Partition 내부의 Message 는 순차적으로 저장 & 소비
- Partition 을 여러개 두어서, Partition 개수만큼 **병렬 처리** 가능
- 동시에 처리되서는 안되는 순차 메세지는 하나의 Partition에, 동시에 처리 가능한 동시성 메세지는 여러 Partition 으로 나누어서 높은 가용성 보장

## Kafka Controller
- Broker 들을 모니터링하고, Leader 파티션 선출, 장애 파악
- 메타데이터 관리

## Consumer Group
- 동일한 Group ID를 가진 Consumer 집합
- 같은 Group에 속한 Consumer들은 서로 다른 파티션을 소비함으로써 **로드 분산**을 수행
- **한 파티션은 오직 하나의 Consumer Group 내에서 하나의 Consumer** 만 소비



# Redis / RabbitMQ / Kafka 차이점
| 항목         | Redis           | RabbitMQ            | Kafka                      |
| ---------- | --------------- | ------------------- | -------------------------- |
| 목적         | 빠르고 간단한 Pub/Sub | 신뢰성 있는 메시지 전달       | 대용량 데이터 스트리밍 & 로그 저장       |
| 메시지 보존     | 구독자 없으면 유실      | 소비 전까지 큐에 저장됨       | 일정 기간 디스크에 저장              |
| 신뢰성        | 낮음 (기본은 비영속)    | 높음 | 매우 높음 (디스크 저장 + offset 추적) |
| 주요 사례      | 채팅, 실시간 알림      | 결제 처리, 트랜잭션 메시지     | 사용자 이벤트 로그, 실시간 분석         |

## Redis Pub/Sub
- 메세지를 구독 중인 클라이언트가 없으면 메세지 유실
- 구독 중인 클라이언트에게만 메세지 전달 - 브로드캐스트 방식
- 메모리 저장
- 메세지 손실 가능성이 높기에, 신뢰성이 떨어짐

## RabbitMQ
- 큐에 저장되고, 소비자가 처리하면 삭제. 메시지 지속성을 설정할 수 있음
- 1개의 큐에 여러 consumer가 있으면 Round-Robin 방식으로 분배
- durable queue + persistent message 설정 시 디스크에 저장
- 신뢰성 있는 메시지 처리 설계 가능
  - 동일한 메세지에 대한 병렬처리는 불가능
- RabbitMQ 가 구독자에게 Push 하는 방식

## Kafka
- 메시지가 지정된 기간 동안 디스크에 저장되고, 여러 소비자가 독립적으로 읽기 가능
- consumer group 개념이 있어 복수 그룹이 같은 메시지를 읽을 수 있음
- 디스크 기반 저장, 매우 내구성 있음. 대량 로그 처리에 적합
- 메시지 유실 없음 (디스크 저장 + offset 기반 읽기)
    - 메세지는 보존 기간(retention) 동안 디스크에 남아 있음
    - Consumer는 각자 자신의 offset을 관리해서, "내가 어디까지 읽었는가"를 기준으로 메시지를 읽음
- Consumer 가 Message 를 pull 하는 방식이기에, 서버 부하 발생시, Consumer 가 알아서 메세지 처리량을 조절 할 수 있음


# 카프카가 메시지를 중복처리하지 않게하는 방법
## 카프카에서 메세지 중복 처리가 발생하는 원인
| 원인               | 설명                                |
| ---------------- | --------------------------------- |
| Consumer가 메시지 처리 전에 죽음 | offset이 commit되지 않아서 다시 같은 메시지를 읽음 |
| 수동 commit 실패     | 메시지는 처리됐지만 offset commit은 실패 → 재처리됨 |
| 네트워크 재시도         | Consumer가 동일 메시지를 두 번 이상 받는 경우    |
| Exactly-Once 미사용 | 기본은 at-least-once 이므로 중복 가능성 존재   |

## 예방 방법
- 기본적으로 Producer / Consumer 양측에 동시 처리 필요
1. Consumer 에서 Idempotent 처리
    - 같은 메시지를 두 번 처리해도 결과가 같도록 만드는 것
   ```java
    @KafkaListener(topics = "orders", groupId = "order-consumer")
    public void handle(OrderMessage message) {
        if (orderRepository.existsByOrderId(message.getOrderId())) {
            log.info("Duplicate message: already processed.");
            return;
        }
        orderRepository.save(message.toEntity());
    }
    ```
2. 수동 offset commit 사용
    - 메시지 정상 처리된 후에만 commit 되므로 안정적
    - 자동 commit 을 사용시, 아직 처리도 안 했는데 commit 되는 경우 발생 가능
3. Exactly Once Semantics (EOS)
    - Kafka 의 Producer 측에서는 **Exactly Once** 를 지원
    - Consumer 에서는 별도 설정 + 설계가 필요
    ```yml
   spring:
     kafka:
       producer:
         transaction-id-prefix: tx-  // Exactly Once 설정
   ```
   **Consumer 에서는 Transaction 으로 처리**
   ```java
    @KafkaListener(...)
    @Transactional
    public void listen(String message, Acknowledgment ack) {
        process(message);       // 처리 완료 후
        ack.acknowledge();      // 수동 커밋
    }
   ```
   - Kafka 와 DB를 동일 트랜잭션으로 묶는 구조
   - 복잡하지만 가장 확실한 방식 중 하나
   - 성능 저하 트레이드 오프 고려 필요
4. Producer 측에서 중복 방지 설정
    ```
   spring:
     kafka:
       producer:
         enable-idempotence: true
   ```
   - Kafka 가 같은 메시지 키 + seq로 중복 전송하는 걸 감지해 자동 중복 방지
   - Producer 중복 방지 ≠ Consumer 중복 방지 임을 기억 해야 함


# 카프카가 메시지를 유실처리하지 않게하는 방법들
### 1. Producer 측 메시지 유실 방지
| 설정 / 방법                                     | 설명                                                           |
| ------------------------------------------- | ------------------------------------------------------------ |
| acks=all                                    | 모든 ISR(in-sync replica)에 복제되기 전까지는 메시지를 성공으로 간주하지 않음 → 가장 안전 |
| retries > 0                                | 전송 실패 시 재시도 가능 (일시적 네트워크 오류 등 대비)                            |
| enable.idempotence=true                    | 중복 방지를 포함한 안정 전송                        |
| linger.ms, batch.size 등                    | 튜닝용. 성능 최적화와 유실 방지의 밸런스 조정                                   |
| max.in.flight.requests.per.connection = 1 | 순서 유지를 위한 옵션, retry 시 중복 방지에 도움                              |

### 2. Broker 측 유실 방지
| 항목                                        | 설명                                    |
| ----------------------------------------- | ------------------------------------- |
| replication.factor >= 2                   | 복제본이 하나 이상이면 브로커 장애 시에도 데이터 유지 가능     |
| min.insync.replicas >= 2                  | 최소한 동기화된 복제본 수를 강제 → ISR 수 부족 시 쓰기 거부 |
| unclean.leader.election = false           | 리더가 되지 않은 복제본에서 리더 선출을 막음 → 메시지 손실 방지 |
| log.retention.ms / log.retention.bytes   | 로그 유지 기간 조정 (기한 초과 시 삭제)              |
| log.segment.bytes + log.retention.check.interval.ms | 얼마나 자주 retention 검사를 할지 결정            |

### 3. Consumer 측 유실 방지
| 설정 / 방법                           | 설명                                    |
| --------------------------------- | ------------------------------------- |
| enable.auto.commit = false       | 자동 커밋을 끄고, **메시지 처리 완료 후 수동 커밋**으로 제어 |
| ackMode = MANUAL/MANUAL_IMMEDIATE | Spring Kafka에서 수동 커밋 모드 설정            |
| @Transactional + 트랜잭션 커밋         | DB 작업 + offset commit을 하나의 트랜잭션으로 묶기  |
| 재처리 실패 메시지 → DLQ 처리               | 실패한 메시지를 따로 저장하여 유실 방지                |

### 4. DLQ (Dead Letter Queue) 설정
- 메시지 처리 실패 시 orders.DLQ 와 같은 별도 토픽에 저장
- DLQ 는 운영자 확인 혹은 scheduler 재처리에 활용

