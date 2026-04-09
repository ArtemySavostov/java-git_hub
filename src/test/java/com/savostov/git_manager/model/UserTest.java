package com.savostov.git_manager.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void userCreation_withDefaultConstructor_createsEmptyUser() {
        assertThat(user.getId()).isNull();
        assertThat(user.getUsername()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getPassword()).isNull();
        assertThat(user.getRole()).isNull();
        assertThat(user.getSubscriptions()).isEmpty();
        assertThat(user.getSubscribers()).isEmpty();
    }

    @Test
    void setId_withValidId_setsId() {
        user.setId(1L);
        assertThat(user.getId()).isEqualTo(1L);
    }

    @Test
    void setUsername_withValidUsername_setsUsername() {
        user.setUsername("testuser");
        assertThat(user.getUsername()).isEqualTo("testuser");
    }

    @Test
    void setEmail_withValidEmail_setsEmail() {
        user.setEmail("test@example.com");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void setPassword_withValidPassword_setsPassword() {
        user.setPassword("password123");
        assertThat(user.getPassword()).isEqualTo("password123");
    }

    @Test
    void setRole_withValidRole_setsRole() {
        user.setRole("USER");
        assertThat(user.getRole()).isEqualTo("USER");
    }

    @Test
    void subscriptions_withEmptySet_initializesEmpty() {
        Set<User> subscriptions = user.getSubscriptions();
        assertThat(subscriptions).isNotNull();
        assertThat(subscriptions).isEmpty();
    }

    @Test
    void subscriptions_withSet_setsSubscriptions() {
        Set<User> subscriptions = new HashSet<>();
        User subscribedUser = new User();
        subscribedUser.setId(2L);
        subscriptions.add(subscribedUser);
        
        user.setSubscriptions(subscriptions);
        assertThat(user.getSubscriptions()).isEqualTo(subscriptions);
    }

    @Test
    void subscribers_withEmptySet_initializesEmpty() {
        Set<User> subscribers = user.getSubscribers();
        assertThat(subscribers).isNotNull();
        assertThat(subscribers).isEmpty();
    }

    @Test
    void subscribers_withSet_setsSubscribers() {
        Set<User> subscribers = new HashSet<>();
        User subscriber = new User();
        subscriber.setId(3L);
        subscribers.add(subscriber);
        
        user.setSubscribers(subscribers);
        assertThat(user.getSubscribers()).isEqualTo(subscribers);
    }

    @Test
    void addSubscription_withValidUser_addsToSubscriptions() {
        User subscribedUser = new User();
        subscribedUser.setId(2L);
        
        user.getSubscriptions().add(subscribedUser);
        
        assertThat(user.getSubscriptions()).contains(subscribedUser);
    }

    @Test
    void removeSubscription_withValidUser_removesFromSubscriptions() {
        User subscribedUser = new User();
        subscribedUser.setId(2L);
        
        user.getSubscriptions().add(subscribedUser);
        user.getSubscriptions().remove(subscribedUser);
        
        assertThat(user.getSubscriptions()).doesNotContain(subscribedUser);
    }

    @Test
    void addSubscriber_withValidUser_addsToSubscribers() {
        User subscriber = new User();
        subscriber.setId(3L);
        
        user.getSubscribers().add(subscriber);
        
        assertThat(user.getSubscribers()).contains(subscriber);
    }

    @Test
    void removeSubscriber_withValidUser_removesFromSubscribers() {
        User subscriber = new User();
        subscriber.setId(3L);
        
        user.getSubscribers().add(subscriber);
        user.getSubscribers().remove(subscriber);
        
        assertThat(user.getSubscribers()).doesNotContain(subscriber);
    }

    @Test
    void equals_withSameObject_returnsTrue() {
        assertThat(user.equals(user)).isTrue();
    }

    @Test
    void equals_withNull_returnsFalse() {
        assertThat(user.equals(null)).isFalse();
    }

    @Test
    void equals_withSameId_returnsTrue() {
        User other = new User();
        user.setId(1L);
        other.setId(1L);
        
        assertThat(user.equals(other)).isTrue();
    }

    @Test
    void equals_withDifferentId_returnsFalse() {
        User other = new User();
        user.setId(1L);
        other.setId(2L);
        
        assertThat(user.equals(other)).isFalse();
    }

    @Test
    void equals_withNoId_returnsFalse() {
        User other = new User();
        user.setId(null);
        other.setId(null);
        
        assertThat(user.equals(other)).isFalse();
    }

    @Test
    void hashCode_withSameObject_returnsSameHashCode() {
        int hashCode1 = user.hashCode();
        int hashCode2 = user.hashCode();
        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    void hashCode_withId_returnsIdHashCode() {
        user.setId(1L);
        int hashCode = user.hashCode();
        assertThat(hashCode).isEqualTo(Long.valueOf(1L).hashCode());
    }

    @Test
    void hashCode_withNullId_returnsZero() {
        user.setId(null);
        int hashCode = user.hashCode();
        assertThat(hashCode).isEqualTo(0);
    }

    @Test
    void toString_withValidUser_returnsStringRepresentation() {
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        
        String result = user.toString();
        
        assertThat(result).contains("User");
        assertThat(result).contains("id=1");
        assertThat(result).contains("username=testuser");
        assertThat(result).contains("email=test@example.com");
    }
}
