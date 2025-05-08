<template>
  <a-button type="primary" @click="handleClick">AI 生成题目</a-button>
  <a-drawer
    :width="340"
    :visible="visible"
    @ok="handleOk"
    @cancel="handleCancel"
    unmountOnClose
  >
    <template #title>AI 生成题目</template>
    <div>
      <a-form
        auto-label-width
        :model="form"
        label-align="left"
        @submit="handleSubmit"
      >
        <a-form-item field="questionNumber" label="题目数量">
          <a-input-number v-model="form.questionNumber" min="0" max="20" />
        </a-form-item>
        <a-form-item field="optionNumber" label="选项数">
          <a-input-number v-model="form.optionNumber" min="0" max="6" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button
              :loading="submitting"
              type="primary"
              html-type="submit"
              style="width: 120px"
            >
              {{ submitting ? "生成中" : "一键生成" }}
            </a-button>
            <a-button
              :loading="submitting"
              style="width: 120px"
              @click="handleSSESubmit"
            >
              {{ submitting ? "生成中" : "实时生成" }}
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>
  </a-drawer>
</template>

<script setup lang="ts">
import { defineProps, reactive, ref, withDefaults } from "vue";
import API from "@/api";
import { aiGenerateQuestionUsingPost } from "@/api/questionController";
import message from "@arco-design/web-vue/es/message";

interface Props {
  appId: string;
  onSuccess?: (result: API.QuestionContentDto[]) => void;
  onSSESuccess?: (result: API.QuestionContentDto) => void;
  onSSEStart?: (event: any) => void;
  onSSEEnd?: (event: any) => void;
}

const props = withDefaults(defineProps<Props>(), {
  appId: () => {
    return "";
  },
});

const form = reactive({
  appId: "",
  questionNumber: 2,
  optionNumber: 10,
} as API.AiGenerateQuestionRequest);

const visible = ref(false);
const submitting = ref(false);

const handleClick = () => {
  visible.value = true;
};
const handleOk = () => {
  visible.value = false;
};
const handleCancel = () => {
  visible.value = false;
};

//一键生成
const handleSubmit = async () => {
  if (!props.appId) {
    return;
  }
  let res: any;
  submitting.value = true;
  res = await aiGenerateQuestionUsingPost({
    appId: props.appId,
    questionNumber: form.questionNumber,
    optionNumber: form.optionNumber,
  });
  if (res.data.code === 0 && res.data.data.length > 0) {
    if (props.onSuccess) {
      props.onSuccess(res.data.data);
    } else {
      message.success("生成题目成功");
    }
    visible.value = false;
  } else {
    message.error("操作失败，" + res.data.message);
  }
  submitting.value = false;
};

//实时生成
const handleSSESubmit = async () => {
  if (!props.appId) {
    return;
  }
  let res: any;
  submitting.value = true;
  //创建 SSE 连接
  const eventSource = new EventSource(
    //todo 手动填写完整的后端地址
    "http://localhost:8101/api/question/ai_generate/sse?appId=" +
      props.appId +
      "&questionNumber=" +
      form.questionNumber +
      "&optionNumber=" +
      form.optionNumber
  );
  //可能会出现重复生成
  // eventSource.onopen = (event) => {
  //   props.onSSEStart?.(event);
  //   handleCancel();
  // };
  let isFirst = true;
  eventSource.onerror = (event) => {
    if (event.eventPhase === EventSource.CLOSED) {
      eventSource.close();
      props.onSSEEnd?.(event);
    }
  };
  eventSource.onmessage = (event) => {
    if (isFirst) {
      props.onSSEStart?.(event);
      handleCancel();
      isFirst = !isFirst;
    }
    props.onSSESuccess?.(JSON.parse(event.data));
  };
  submitting.value = false;
};
</script>
