package repo.build.filter

import repo.build.ActionContext

/**
 * If the predicate returns true then the output does not occur
 */
interface OutputFilter {

    abstract Boolean apply(ActionContext context, List<ByteArrayOutputStream> output)
}
