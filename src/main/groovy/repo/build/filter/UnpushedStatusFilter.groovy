package repo.build.filter

import repo.build.ActionContext


class UnpushedStatusFilter implements OutputFilter {

    Boolean apply(ActionContext context, List<ByteArrayOutputStream> output) {
        if (output.isEmpty()) true
        def temp = output
        def result = temp.findAll({ it.toString() != ""}).findAll({it.toString() != "\n"})
        result.size() == 3
    }
}
