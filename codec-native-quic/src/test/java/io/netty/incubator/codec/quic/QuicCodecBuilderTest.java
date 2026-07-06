/*
 * Copyright 2021 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.incubator.codec.quic;

import io.netty.channel.ChannelHandler;
import io.netty.util.concurrent.ImmediateExecutor;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuicCodecBuilderTest {

    @Test
    void testCopyConstructor() throws IllegalAccessException {
        TestQuicCodecBuilder original = new TestQuicCodecBuilder();
        try {
            init(original);
            TestQuicCodecBuilder copy = new TestQuicCodecBuilder(original);
            assertThat(copy).usingRecursiveComparison().isEqualTo(original);
        } finally {
            freeQuicheConfigs(original);
        }
    }

    @Test
    void testBuildUsesProvidedQuicheConfig() {
        TestQuicCodecBuilder builder = new TestQuicCodecBuilder();
        QuicheConfig config = newQuicheConfig();
        try {
            builder.sslEngineProvider(q -> null);
            builder.quicheConfig(config);

            builder.build();

            assertThat(builder.buildConfig).isSameAs(config);
        } finally {
            config.free();
        }
    }

    @Test
    void testCreateConfigCreatesNewConfig() {
        TestQuicCodecBuilder builder = new TestQuicCodecBuilder();

        QuicheConfig config = builder.createConfig();
        try {
            assertThat(config).isNotNull();
            assertThat(config.nativeAddress()).isNotEqualTo(-1);
        } finally {
            config.free();
        }
    }

    @Test
    void testCreateConfigReturnsProvidedConfig() {
        TestQuicCodecBuilder builder = new TestQuicCodecBuilder();
        QuicheConfig config = newQuicheConfig();
        try {
            builder.quicheConfig(config);

            assertThat(builder.createConfig()).isSameAs(config);
        } finally {
            config.free();
        }
    }

    @Test
    void testBuildDoesNotFreeProvidedQuicheConfigOnFailure() {
        TestQuicCodecBuilder builder = new TestQuicCodecBuilder();
        QuicheConfig config = newQuicheConfig();
        try {
            builder.sslEngineProvider(q -> null);
            builder.quicheConfig(config);
            builder.failBuild = true;

            assertThatThrownBy(builder::build).isInstanceOf(IllegalStateException.class);
            assertThat(config.nativeAddress()).isNotEqualTo(-1);
        } finally {
            config.free();
        }
    }

    private static void init(TestQuicCodecBuilder builder) throws IllegalAccessException {
        Field[] fields = builder.getClass().getSuperclass().getDeclaredFields();
        for (Field field : fields) {
            modifyField(builder, field);
        }
    }

    private static void modifyField(TestQuicCodecBuilder builder, Field field) throws IllegalAccessException {
        field.setAccessible(true);
        Class<?> clazz = field.getType();
        if (Boolean.class == clazz) {
            field.set(builder, Boolean.TRUE);
        } else if (Integer.class == clazz) {
            field.set(builder, Integer.MIN_VALUE);
        } else if (Long.class == clazz) {
            field.set(builder, Long.MIN_VALUE);
        } else if (QuicCongestionControlAlgorithm.class == clazz) {
            field.set(builder, QuicCongestionControlAlgorithm.CUBIC);
        } else if (FlushStrategy.class == clazz) {
            field.set(builder, FlushStrategy.afterNumBytes(10));
        } else if (Function.class == clazz) {
            field.set(builder, Function.identity());
        } else if (QuicheConfig.class == clazz) {
            field.set(builder, newQuicheConfig());
        } else if (boolean.class == clazz) {
            field.setBoolean(builder, true);
        } else if (int.class == clazz) {
            field.setInt(builder, -1);
        } else if (byte[].class == clazz) {
            field.set(builder, new byte[16]);
        } else if (Executor.class == clazz) {
            field.set(builder, ImmediateExecutor.INSTANCE);
        } else {
            throw new IllegalArgumentException("Unknown field type " + clazz);
        }
    }

    private static QuicheConfig newQuicheConfig() {
        return new QuicheConfig(Quiche.QUICHE_PROTOCOL_VERSION, null, null,
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null, null);
    }

    private static void freeQuicheConfigs(TestQuicCodecBuilder builder) throws IllegalAccessException {
        Field[] fields = builder.getClass().getSuperclass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType() == QuicheConfig.class) {
                field.setAccessible(true);
                QuicheConfig config = (QuicheConfig) field.get(builder);
                if (config != null) {
                    config.free();
                }
            }
        }
    }

    private static final class TestQuicCodecBuilder extends QuicCodecBuilder<TestQuicCodecBuilder> {
        private QuicheConfig buildConfig;
        private boolean failBuild;

        TestQuicCodecBuilder() {
            super(true);
        }

        TestQuicCodecBuilder(TestQuicCodecBuilder builder) {
            super(builder);
        }

        @Override
        public TestQuicCodecBuilder clone() {
            // no-op
            return null;
        }

        @Override
        protected ChannelHandler build(
                QuicheConfig config,
                Function<QuicChannel, ? extends QuicSslEngine> sslContextProvider,
                Executor sslTaskExecutor,
                int localConnIdLength,
                FlushStrategy flushStrategy) {
            buildConfig = config;
            if (failBuild) {
                throw new IllegalStateException("failBuild");
            }
            return null;
        }
    }
}
