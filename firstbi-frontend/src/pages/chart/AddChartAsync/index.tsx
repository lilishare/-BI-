// @ts-ignore
import React, {useState} from "react";
// @ts-ignore
// @ts-ignore
import {Button, Card, Form, Input, message, Select, SelectProps, Space, Upload} from "antd";
import {UploadOutlined} from "@ant-design/icons";
import {getChartByThreadUsingPost} from "@/services/firstbi/chartController";
import {useForm} from 'antd/es/form/Form';

const formItemLayout = {
  labelCol: {span: 4},
  wrapperCol: {span: 8},
};

const AddChart: React.FC = () => {
  const options: SelectProps['options'] = [];
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [form] = useForm();
  const handleChange = (value: string) => {
    console.log(`selected ${value}`);
  };

  options.push({
    value: "柱形图",
    label: "柱形图",
  });
  options.push({
    value: "折线图",
    label: "折线图",
  });
  options.push({
    value: "饼图",
    label: "饼图",
  });
  const onFinish = async (values: any) => {
    // 避免重复提交
    if (submitting) {
      return;
    }
    setSubmitting(true);
    // 对接后端，上传数据
    let chartTypeStr = "";

    // eslint-disable-next-line eqeqeq
    if (values.chartType != null) {
      values.chartType.forEach((tag: string) => {
        chartTypeStr += tag;
        chartTypeStr += " ";
      })
    }

    const params = {
      ...values,
      chartType: chartTypeStr,
      file: undefined,
    };

    try {
      const res = await getChartByThreadUsingPost(params, {}, values.file.file.originFileObj);
      if (!res?.data) {
        message.error('分析失败');
      } else {
        message.success('分析任务提交成功，稍后请在我的图表页面查看');
        form.resetFields();
      }
    } catch (e: any) {
      message.error('分析失败，' + e.message);
    }
    setSubmitting(false);
  }

  return (
    <div className="add-chart">
      <Card title="智能分析">
        <Form
          form={form}
          name="addChart"
          onFinish={onFinish}
          labelAlign="left"
          labelCol={{span: 4}}
          wrapperCol={{span: 16}}
        >
          <Form.Item
            label="分析目标"
            name="goal"
            rules={[{required: true, message: 'Please input!'}]}
          >
            <Input.TextArea placeholder="请输入分析目标"/>
          </Form.Item>
          <Form.Item
            {...formItemLayout}
            name="chartName"
            label="图表名称"
          >
            <Input placeholder="请输入图表名称"/>
          </Form.Item>
          <Form.Item
            label="图标类型"
            name="chartType"
          >
            <Select
              mode="tags"
              style={{width: '100%'}}
              placeholder="例如：柱形图"
              onChange={handleChange}
              options={options}
            />
          </Form.Item>

          <Form.Item
            name="file"
            label="上传Excel文件"
          >
            <Upload name="file" maxCount={1}>
              <Button icon={<UploadOutlined/>}>点击选择文件</Button>
            </Upload>
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                提交
              </Button>
              <Button htmlType="reset">重置输入</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};
export default AddChart;
