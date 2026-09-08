package org.openntf.drapi.meta;

import org.openntf.drapi.internal.meta.ColumnImpl;

/**
 * Represents a column in a view. Currently, it's pretty much a marker interface.
 */
public sealed interface Column extends ValueHolder permits ColumnImpl {

}
