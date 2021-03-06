/**
 * Copyright (C) 2018-2020 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.core.events;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.test.context.junit4.SpringRunner;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.repositories.ProducerBindingRepository;
import com.expediagroup.streamplatform.streamregistry.core.services.ProducerBindingService;
import com.expediagroup.streamplatform.streamregistry.core.validators.ProducerBindingValidator;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Specification;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfig.class)
public class NotificationEventEmitterProducerBindingServiceTest {

  @MockBean
  private ApplicationEventMulticaster applicationEventMulticaster;

  @MockBean
  private HandlerService handlerService;

  @MockBean
  private ProducerBindingValidator producerBindingValidator;

  @MockBean
  private ProducerBindingRepository producerBindingRepository;

  private NotificationEventEmitter<ProducerBinding> producerBindingServiceEventEmitter;
  private ProducerBindingService producerBindingService;

  @Before
  public void before() {
    producerBindingServiceEventEmitter = Mockito.spy(DefaultNotificationEventEmitter.<ProducerBinding>builder()
        .classType(ProducerBinding.class)
        .applicationEventMulticaster(applicationEventMulticaster)
        .build());

    producerBindingService = Mockito.spy(new ProducerBindingService(handlerService, producerBindingValidator, producerBindingRepository, producerBindingServiceEventEmitter));
  }

  @Test
  public void givenAProducerBindingForCreate_validateThatNotificationEventIsEmitted() {
    final ProducerBinding entity = getDummyProducerBinding();
    final EventType type = EventType.CREATE;
    final String source = producerBindingServiceEventEmitter.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
    final NotificationEvent<ProducerBinding> event = getDummyNotificationEvent(source, type, entity);

    Mockito.when(producerBindingRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    Mockito.doNothing().when(producerBindingValidator).validateForCreate(entity);
    Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());
    Mockito.when(producerBindingRepository.save(entity)).thenReturn(entity);
    Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

    producerBindingService.create(entity);

    Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
        .multicastEvent(event);

    Mockito.verify(producerBindingServiceEventEmitter, Mockito.timeout(1000).times(0))
        .onFailedEmitting(Mockito.any(), Mockito.eq(event));
  }

  @Test
  public void givenAProducerBindingForUpdate_validateThatNotificationEventIsEmitted() {
    final ProducerBinding entity = getDummyProducerBinding();
    final EventType type = EventType.UPDATE;
    final String source = producerBindingServiceEventEmitter.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
    final NotificationEvent<ProducerBinding> event = getDummyNotificationEvent(source, type, entity);

    Mockito.when(producerBindingRepository.findById(Mockito.any())).thenReturn(Optional.of(entity));
    Mockito.doNothing().when(producerBindingValidator).validateForUpdate(Mockito.eq(entity), Mockito.any());
    Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), Mockito.any())).thenReturn(getDummySpecification());

    Mockito.when(producerBindingRepository.save(entity)).thenReturn(entity);
    Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

    producerBindingService.update(entity);

    Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
        .multicastEvent(event);

    Mockito.verify(producerBindingServiceEventEmitter, Mockito.timeout(1000).times(0))
        .onFailedEmitting(Mockito.any(), Mockito.eq(event));
  }

  @Test
  public void givenANullProducerBindingRetrievedByRepositoryForCreate_validateThatNotificationEventIsNotEmitted() {
    final ProducerBinding entity = getDummyProducerBinding();

    Mockito.when(producerBindingRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    Mockito.doNothing().when(producerBindingValidator).validateForCreate(entity);
    Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());

    Mockito.when(producerBindingRepository.save(entity)).thenReturn(null);
    Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(Mockito.any());

    producerBindingService.create(entity);

    Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(0))
        .multicastEvent(Mockito.any());

    Mockito.verify(producerBindingServiceEventEmitter, Mockito.timeout(1000).times(0))
        .onFailedEmitting(Mockito.any(), Mockito.any());
  }

  @Test
  public void givenANullProducerBindingRetrievedByRepositoryForUpdate_validateThatNotificationEventIsNotEmitted() {
    final ProducerBinding entity = getDummyProducerBinding();

    Mockito.when(producerBindingRepository.findById(Mockito.any())).thenReturn(Optional.of(entity));
    Mockito.doNothing().when(producerBindingValidator).validateForUpdate(Mockito.eq(entity), Mockito.any());
    Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), Mockito.any())).thenReturn(getDummySpecification());

    Mockito.when(producerBindingRepository.save(entity)).thenReturn(null);
    Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(Mockito.any());

    producerBindingService.update(entity);

    Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(0))
        .multicastEvent(Mockito.any());

    Mockito.verify(producerBindingServiceEventEmitter, Mockito.timeout(1000).times(0))
        .onFailedEmitting(Mockito.any(), Mockito.any());
  }

  @Test
  public void givenAProducerBindingForUpsert_validateThatNotificationEventIsEmitted() {
    final ProducerBinding entity = getDummyProducerBinding();
    final EventType type = EventType.UPDATE;
    final String source = producerBindingServiceEventEmitter.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
    final NotificationEvent<ProducerBinding> event = getDummyNotificationEvent(source, type, entity);

    Mockito.when(producerBindingRepository.findById(Mockito.any())).thenReturn(Optional.of(entity));
    Mockito.doNothing().when(producerBindingValidator).validateForUpdate(Mockito.eq(entity), Mockito.any());
    Mockito.when(handlerService.handleUpdate(Mockito.eq(entity), Mockito.any())).thenReturn(getDummySpecification());

    Mockito.when(producerBindingRepository.save(entity)).thenReturn(entity);
    Mockito.doNothing().when(applicationEventMulticaster).multicastEvent(event);

    producerBindingService.upsert(entity);

    Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
        .multicastEvent(event);

    Mockito.verify(producerBindingServiceEventEmitter, Mockito.timeout(1000).times(0))
        .onFailedEmitting(Mockito.any(), Mockito.eq(event));
  }

  @Test
  public void givenAProducerBindingForCreate_handleAMulticasterException() {
    final ProducerBinding entity = getDummyProducerBinding();
    final EventType type = EventType.CREATE;
    final String source = producerBindingServiceEventEmitter.getSourceEventPrefix(entity).concat(type.toString().toLowerCase());
    final NotificationEvent<ProducerBinding> event = getDummyNotificationEvent(source, type, entity);

    Mockito.when(producerBindingRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    Mockito.doNothing().when(producerBindingValidator).validateForCreate(entity);
    Mockito.when(handlerService.handleInsert(entity)).thenReturn(getDummySpecification());

    Mockito.when(producerBindingRepository.save(entity)).thenReturn(entity);
    Mockito.doThrow(new RuntimeException("BOOOOOOOM")).when(applicationEventMulticaster).multicastEvent(event);

    Optional<ProducerBinding> response = producerBindingService.create(entity);

    Mockito.verify(applicationEventMulticaster, Mockito.timeout(1000).times(1))
        .multicastEvent(event);

    Mockito.verify(producerBindingServiceEventEmitter, Mockito.timeout(1000).times(1))
        .onFailedEmitting(Mockito.any(), Mockito.eq(event));

    Assert.assertTrue(response.isPresent());
    Assert.assertEquals(response.get(), entity);
  }

  public <T> NotificationEvent<T> getDummyNotificationEvent(String source, EventType type, T entity) {
    return NotificationEvent.<T>builder()
        .source(source)
        .eventType(type)
        .entity(entity)
        .build();
  }

  public ProducerBinding getDummyProducerBinding() {
    final ProducerBinding entity = new ProducerBinding();

    return entity;
  }

  public Specification getDummySpecification() {
    Specification spec = new Specification();
    spec.setConfigJson("{}");
    spec.setDescription("dummy spec");

    return spec;
  }
}