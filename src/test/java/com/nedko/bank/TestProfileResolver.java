package com.nedko.bank;

import org.springframework.test.context.ActiveProfilesResolver;

public final class TestProfileResolver implements ActiveProfilesResolver {

    @Override
    public String[] resolve(final Class<?> testClass) {
        return new String[] { "test" };
    }
}
