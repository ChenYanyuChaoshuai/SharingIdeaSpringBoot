/*
底部栏组件
 */
var footerTemplate = `
<el-footer>
        <el-row type="flex" class="row-bg" justify="center">
            <el-col :span="4"><div class="grid-content bg-purple"></div></el-col>
            <el-col :span="4"><div class="grid-content bg-purple-light"></div></el-col>
            <el-col :span="4"><div class="grid-content bg-purple"></div></el-col>
        </el-row>
</el-footer>
`

var footerModule = {
    data:function () {
        return{

        }
    },
    props:[],
    template: footerTemplate,
    methods:{

    },

}

Vue.component("my_footer",footerModule);