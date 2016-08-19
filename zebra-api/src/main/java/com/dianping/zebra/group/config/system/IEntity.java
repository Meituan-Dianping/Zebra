package com.dianping.zebra.group.config.system;

public interface IEntity<T> {
   public void accept(IVisitor visitor);

   public void mergeAttributes(T other);

}
