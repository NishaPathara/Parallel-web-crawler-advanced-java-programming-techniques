package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;
  private final Object target;
  private final ProfilingState state;
  private final ZonedDateTime startTime;

  // TODO: You will need to add more instance fields and constructor arguments to this class.
  ProfilingMethodInterceptor(Clock clock,Object target, ProfilingState state, ZonedDateTime startTime) {
    this.clock = Objects.requireNonNull(clock);
    this.target = Objects.requireNonNull(target);
    this.state = Objects.requireNonNull(state);
    this.startTime = Objects.requireNonNull(startTime);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // TODO: This method interceptor should inspect the called method to see if it is a profiled
    //       method. For profiled methods, the interceptor should record the start time, then
    //       invoke the method using the object that is being profiled. Finally, for profiled
    //       methods, the interceptor should record how long the method call took, using the
    //       ProfilingState methods.
    Object invoked;
    Instant startInstant = null;
    boolean is_profiled = false;
    if (method.getAnnotation(Profiled.class) != null) {
      is_profiled = true;
    }
    if (is_profiled==true) {
      startInstant = clock.instant();
      try {
        invoked = method.invoke(target, args);
      } catch (InvocationTargetException ex) {
        throw ex.getTargetException();
      } catch (IllegalAccessException ex) {
        throw new RuntimeException(ex);
      } finally {
        Duration duration = Duration.between(startInstant, clock.instant());
        state.record(target.getClass(), method, duration);
      }
    }else{
      try {
        invoked = method.invoke(target, args);
      } catch (InvocationTargetException ex) {
        throw ex.getTargetException();
      } catch (IllegalAccessException ex) {
        throw new RuntimeException(ex);
      }
    }
    return invoked;
  }
  @Override
  public boolean equals(Object object) {
    if(object == this) {
      return true;
    }
    if(!(object instanceof ProfilingMethodInterceptor)) {
      return false;
    }
    ProfilingMethodInterceptor methodInterceptor = (ProfilingMethodInterceptor) object;
    return  methodInterceptor.clock.equals(this.clock) &&
            methodInterceptor.target.equals(this.target) &&
            methodInterceptor.state.equals(this.state) &&
            methodInterceptor.startTime.equals(this.startTime);
  }
}

