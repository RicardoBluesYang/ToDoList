<template>
    <div id="app">
      <h1>ToDoList</h1>
      <!-- <ToDoList /> 移除直接引入的组件，改用 router-view -->
      <router-view></router-view>
    </div>
  </template>


<script>
// import ToDoList from './components/ToDoList.vue' // 移除引入

export default {
    name:'App',
    // components:{
    //     ToDoList
    // },

};

</script>
<style src="./index.css"></style>
