/**
 * Copyright (C) 2018-2019 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.it;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.apollographql.apollo.api.Mutation;

import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertProducerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ProducerBindingQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ProducerBindingsQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateProducerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateProducerBindingStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertProducerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ProducerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ProducerBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.ObjectIT;

public class ProducerBindingIT extends ObjectIT {

  @Override
  public void create() {

    setFactorySuffix("create");

    Mutation insertMutation = factory.insertProducerBindingMutationBuilder().build();

    InsertProducerBindingMutation.Insert insert =
        ((InsertProducerBindingMutation.Data) client.getData(insertMutation))
            .getProducerBinding().getInsert();

    assertThat(insert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(insert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));

    assertMutationFails(insertMutation);
  }

  @Override
  public void update() {

    setFactorySuffix("update");

    Mutation updateMutation = factory.updateProducerBindingMutationBuilder().build();

    assertMutationFails(updateMutation);

    client.invoke(factory.insertProducerBindingMutationBuilder().build());

    Object data = client.getData(updateMutation);
    UpdateProducerBindingMutation.Update update = ((UpdateProducerBindingMutation.Data) data).getProducerBinding().getUpdate();

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void upsert() {

    Object data = client.getData(factory.upsertProducerBindingMutationBuilder().build());

    UpsertProducerBindingMutation.Upsert upsert = ((UpsertProducerBindingMutation.Data) data).getProducerBinding().getUpsert();

    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(upsert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void updateStatus() {
    client.getData(factory.upsertProducerBindingMutationBuilder().build());
    Object data = client.getData(factory.updateProducerBindingStatusBuilder().build());

    UpdateProducerBindingStatusMutation.UpdateStatus update =
        ((UpdateProducerBindingStatusMutation.Data) data).getProducerBinding().getUpdateStatus();

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getStatus().get().getAgentStatus().get("skey").asText(), is("svalue"));
  }

  @Override
  public void queryByKey() {

    ProducerBindingKeyInput input = factory.producerBindingKeyInputBuilder().build();

    try {
      client.getData(ProducerBindingQuery.builder().key(input).build());
    } catch (RuntimeException e) {
      assertEquals(e.getMessage(), "No value present");
    }

    client.getData(factory.upsertProducerBindingMutationBuilder().build());

    ProducerBindingQuery.Data after = (ProducerBindingQuery.Data) client.getData(ProducerBindingQuery.builder().key(input).build());

    assertEquals(after.getProducerBinding().getByKey().getKey().getStreamDomain(), input.streamDomain());
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("query_by_regex");

    ProducerBindingKeyQuery query = ProducerBindingKeyQuery.builder().producerNameRegex("producerName.*").build();

    ProducerBindingsQuery.Data before = (ProducerBindingsQuery.Data) client.getData(ProducerBindingsQuery.builder().key(query).build());

    client.invoke(factory.upsertProducerBindingMutationBuilder().build());

    ProducerBindingsQuery.Data after = (ProducerBindingsQuery.Data) client.getData(ProducerBindingsQuery.builder().key(query).build());

    assertEquals(after.getProducerBinding().getByQuery().size(), before.getProducerBinding().getByQuery().size() + 1);
  }

  @Override
  public void createRequiredDatastoreState() {
    client.createProducer(factory);
  }
}