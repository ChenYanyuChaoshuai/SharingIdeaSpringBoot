/*
文件上传下载记录组件
 */
var colParagraphListTemplate = `
<div>
    <ul>
        <template v-for="(col_paragraph,index) in col_paragraph_views" >
            <li style="list-style: none">
                <my_col_paragraph :col_paragraph="col_paragraph" :index="index"></my_col_paragraph>
            </li>
        </template>
    </ul>
</div>
`

var colParagraphListModule = {
    data:function () {
        return{
            col_paragraph_views:[
                // {
                //     sectionName:"第一节",
                //     courseName:"测试md",
                //     paragraphContent:"图片的描述”功能暂未计划开发，请用“#”代替，如1.1.2.结构框图中图片。图片要**单独**一段。",
                //     colCount:100
                // },
                // {
                //     sectionName:"第一节",
                //     courseName:"测试md",
                //     paragraphContent:"图片的描述”功能暂未计划开发，请用“#”代替，如1.1.2.结构框图中图片。图片要**单独**一段。",
                //     colCount:100
                // },
                // {
                //     sectionName:"第一节",
                //     courseName:"测试md",
                //     paragraphContent:"图片的描述”功能暂未计划开发，请用“#”代替，如1.1.2.结构框图中图片。图片要**单独**一段。",
                //     colCount:100
                // }
            ]
        }
    },
    props:[],
    template: colParagraphListTemplate,
    created:function(){
        this.getColParagraphViews();
    },
    methods:{
        getColParagraphViews:function () {
            var _this = this;
            axios.get("paragraph/getColParagraphViews",{
                params:{
                    uid:0,
                    cid:100,
                    page:1
                },
                withCredentials:true
            })
                .then(function(response){
                    _this.col_paragraph_views = response.data.data.colParagraphViews;
                })
                .catch(function(err){
                    console.log(err);
                })
        }
    },

}

Vue.component("my_col_paragarph_list",colParagraphListModule);