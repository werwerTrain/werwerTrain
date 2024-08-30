package com.buaa.werwertrain.controller;

import com.buaa.werwertrain.DTO.OrderDTO;
import com.buaa.werwertrain.DTO.PassengerDTO;
import com.buaa.werwertrain.client.FoodClient;
import com.buaa.werwertrain.client.MessageClient;
import com.buaa.werwertrain.client.OrderClient;
import com.buaa.werwertrain.client.UserClient;
import com.buaa.werwertrain.entity.*;
import com.buaa.werwertrain.service.IPassengerService;
import com.buaa.werwertrain.service.ITrainService;
import com.buaa.werwertrain.service.Impl.EmailService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

//@CrossOrigin(origins = "*",allowCredentials="true",allowedHeaders = "*",methods = {POST,GET})
@RestController
public class TrainController {
    @Autowired
    private ITrainService trainService;
//    @Autowired
//    private IOrderService orderService;
//    @Autowired
//    private IMessageService messageService;
    @Autowired
    private EmailService emailService;
//    @Autowired
//    private IUserService userService;
//    @Autowired
//    private IFoodService foodService;
    @Autowired
    private OrderClient orderClient;
    @Autowired
    private MessageClient messageClient;
    @Autowired
    private UserClient userClient;
    @Autowired
    private FoodClient foodClient;

    private CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
            .enableAutomaticTransitionFromOpenToHalfOpen()
            .failureRateThreshold(20)
            .build());

    // 微服务接口
    @GetMapping("/train/{tid}/{date}/{userID}")
    @CircuitBreaker(name = "getTrainOrder", fallbackMethod = "fallbackGetTrainOrder")
    public List<TrainOrder> getTrainOrderByTrainAndIdentification(
            @PathVariable String tid,
            @PathVariable String date,
            @PathVariable String userID
    ) {
        return trainService.getTrainOrderByTrainAndIdentification(tid, date, userID);
    }

    public List<TrainOrder> fallbackGetTrainOrder(String tid, String date, String userID, Throwable throwable) {
        System.out.println("Get train orders request failed, fallback method executed.");
        return new ArrayList<>();
    }

    @GetMapping("/getTrainIdAndDate/{orderId}")
    @CircuitBreaker(name = "getTrainIdAndDate", fallbackMethod = "fallbackGetTrainIdAndDate")
    public List<Map<String, Object>> getTrainIdAndDate(@PathVariable String orderId) {
        return trainService.getTrainIdAndDate(orderId);
    }

    public List<Map<String, Object>> fallbackGetTrainIdAndDate(String orderId, Throwable throwable) {
        System.out.println("Get train request failed, fallback method executed.");
        return new ArrayList<>();
    }

    @GetMapping("/getStartTime/{trainId}/{trainDate}")
    @CircuitBreaker(name = "getStartTime", fallbackMethod = "fallbackGetStartTime")
    public Map<String,Object> getStartTime(@PathVariable String trainId, @PathVariable String trainDate) {
        return trainService.getStartTime(trainId, trainDate);
    }

    public Map<String,Object> fallbackGetStartTime (String tid, String date, Throwable throwable) {
        System.out.println("Get start time request failed, fallback method executed.");
        return new HashMap<>();
    }
    @GetMapping("/getTrainState/{trainId}/{trainDate}")
    @CircuitBreaker(name = "getTrainState", fallbackMethod = "fallbackGetTrainState")
    public Boolean getTrainState(@PathVariable String trainId,@PathVariable String trainDate) {
        return trainService.getTrainState(trainId, trainDate);
    }

    public Boolean fallbackGetTrainState (String tid, String date, Throwable throwable) {
        System.out.println("Get train state request failed, fallback method executed.");
        return Boolean.FALSE;
    }
    @GetMapping("train/{tid}/{date}")
    @CircuitBreaker(name = "getTrain", fallbackMethod = "fallbackGetTrain")
    public Train getTrainByTidAndDate(
            @PathVariable String tid,
            @PathVariable String date
    ) {
        return trainService.getTrainByTidAndDate(tid, date);
    }

    public Train fallbackGetTrain (String tid, String date, Throwable throwable) {
        System.out.println("Get train request failed, fallback method executed.");
        return new Train();
    }

    // isGD:0高铁 1火车 2全选
    // sort_type:1start_time升序,2start_tiem降序,3duration升序
    // seat_type:
    // is_Hide: true隐藏冲突列车
    @PostMapping("/trains/{start_city}/{arrive_city}/{date}/{userID}")
    public Map<String, Object> trainQuery(
            @PathVariable String start_city,
            @PathVariable String arrive_city,
            @PathVariable String date,
            @PathVariable String userID,
            @RequestParam(value = "is_GD", defaultValue = "2") Integer is_GD,
            @RequestParam(value = "sort_type", defaultValue = "1") Integer sort_type,
            @RequestParam(value = "seat_type", defaultValue = "true,true,true,true,true,true") List<Boolean> seat_type,
            @RequestParam(value = "isHide", defaultValue = "true") Boolean isHide) {
        // 解码路径变量
        start_city = URLDecoder.decode(start_city, StandardCharsets.UTF_8);
        arrive_city = URLDecoder.decode(arrive_city, StandardCharsets.UTF_8);
//        System.out.println("input"+start_city+arrive_city);
//        System.out.println("start end: " + URLEncoder.encode("上海", StandardCharsets.UTF_8) +"/"+ URLEncoder.encode("北京",StandardCharsets.UTF_8));

        List<Train> trains = trainService.searchTrain(start_city, arrive_city, date,
                is_GD, sort_type, seat_type);
        List<Object> result = new ArrayList<>();
        for (Train e : trains) {
            // 如果所选类型都无票则不显示
            boolean[] haveTicketsToShow = {false};
            if (seat_type.get(0) && e.getBusinessSeatSurplus() > 0) {
                haveTicketsToShow[0] = true;
            } else if (seat_type.get(1) && e.getFirstClassSeatSurplus() > 0) {
                haveTicketsToShow[0] = true;
            } else if (seat_type.get(2) && e.getSecondClassSeatSurplus() > 0) {
                haveTicketsToShow[0] = true;
            } else if (seat_type.get(3) && e.getSoftSleeperSurplus() > 0) {
                haveTicketsToShow[0] = true;
            } else if (seat_type.get(4) && e.getHardSleeperSurplus() > 0) {
                haveTicketsToShow[0] = true;
            } else if (seat_type.get(5) && e.getHardSeatSurplus() > 0) {
                haveTicketsToShow[0] = true;
            }
            if (!haveTicketsToShow[0]) {
                continue;
            }


            if (isHide) {
                // 隐藏冲突
                boolean[] conflict = {false};
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                List<TrainOrder> trainOrders = trainService.getTrainOrderByTrainAndIdentification(e.getTrainId(),
                        formatter.format(e.getDate()), userID);
                for (TrainOrder to : trainOrders) {
                    if (orderClient.getOrder(to.getOid()).getOrderStatus().equals("Paid")) {
                        conflict[0] = true;
                    }
                }
                if (conflict[0]) {
                    continue;
                }
            }

            result.add(new HashMap<>() {{
                String trainId = e.getTrainId();
                put("tid", trainId);
                put("start_time", e.getStartTime());
                put("arrive_time", e.getArrivalTime());

                put("time", e.getDuration().toString());
                put("start_station", e.getStartStation());
                put("arrive_station", e.getArrivalStation());


                put("business", new HashMap<>() {{
                    put("price", e.getBusinessSeatPrice());
                    put("remain", e.getBusinessSeatSurplus());
                }});


                put("one", new HashMap<>() {{
                    put("price", e.getFirstClassSeatPrice());
                    put("remain", e.getFirstClassSeatSurplus());
                }});


                put("two", new HashMap<>() {{
                    put("price", e.getSecondClassSeatPrice());
                    put("remain", e.getSecondClassSeatSurplus());
                }});


                put("soft_sleeper", new HashMap<>() {{
                    put("price", e.getSoftSleeperPrice());
                    put("remain", e.getSoftSleeperSurplus());
                }});


                put("hard_sleeper", new HashMap<>() {{
                    put("price", e.getHardSleeperPrice());
                    put("remain", e.getHardSleeperSurplus());
                }});


                put("hard_seat", new HashMap<>() {{
                    put("price", e.getHardSeatPrice());
                    put("remain", e.getHardSeatSurplus());
                }});


                List<Object> stationInfo = new ArrayList<>();
                trainService.searchStopover(trainId).forEach(e -> {
                    stationInfo.add(new HashMap<>() {{
                        put("id", e.getStationId());
                        put("name", e.getStationName());
                        put("arrive", e.getArriveTime());
                        put("departure", e.getLeaveTime());
                        put("stop", e.getDuration());
                    }});
                });
                put("station_info", stationInfo);
            }});
        }
        return new HashMap<>() {{
            put("result", result);
        }};
    }

    /**
     * 提交火车订单并支付
     *
     * @param map 包含订单信息的Map对象，包括用户ID(userId)、火车ID(tid)、乘车日期(date)、乘客信息(person)等
     * @return 返回包含提交结果信息的Map对象，如果下单成功则包含"info": "下单成功！"；如果下单失败则包含"info": "下单失败"
     *
     */
    @PostMapping("/ticket/bill")
    public Map<String, Object> submitTrainOrder(@RequestBody Map<String, Object> map) {
        List<Map<String, String>> persons = (List<Map<String, String>>) map.get("person");
        String userId = (String) map.get("userID");
        String trainId = (String) map.get("tid");
        String trainDate = (String) map.get("date");
        //String billTime = (String) map.get("bill_time");
        Double total = Double.parseDouble((String) map.get("sum_price"));

        String oid = OrderDTO.generateOrderId();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String formattedDate = formatter.format(date);


        int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0;
        for (Map<String, String> person : persons) {
            for(TrainOrder trainOrder: trainService.getTrainOrderByTrainAndIdentification(trainId, trainDate, person.get("identification"))) {
                if (orderClient.getOrder(trainOrder.getOid()).getOrderStatus().equals("Paid")) {
                    return new HashMap<>() {{
                        put("info", "下单失败");
                    }};
                }
            }
        }
        orderClient.addOrder(new OrderDTO(oid, userId, formattedDate, total, "Paid", "Train"));

        for (Map<String, String> person : persons) {
            String type = person.get("seat_type");
            trainService.addTrainOrderDetail(oid, trainId, trainDate, person.get("name"), person.get("identification"), type);
            switch (type) {
                case "商务座":
                    num1 += 1;
                    break;
                case "一等座":
                    num2 += 1;
                    break;
                case "二等座":
                    num3 += 1;
                    break;
                case "软卧":
                    num4 += 1;
                    break;
                case "硬卧":
                    num5 += 1;
                    break;
                case "硬座":
                    num6 += 1;
                    break;
            }
        }
        //火车数量--
        trainService.updateTrainSeat(trainId, trainDate, num1, num2, num3, num4, num5, num6);


//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date date = new Date();
//        String formattedDate = formatter.format(date);

        Map<String, Object> trainMap = trainService.getTrainByIdAndDate(trainId, trainDate);
        LocalDateTime startTime = (LocalDateTime) trainMap.get("startTime");
        String formattedStartTime = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String content = "【WerwerTrip】您已成功购买" + trainDate + "由" + trainMap.get("startStation") + "站发往" + trainMap.get("arrivalStation") + "站的" + trainId + "次列车车票，发车时间" + formattedStartTime + "。请合理安排出行时间。";
        String Mcontent = "您已成功购买" + trainDate + "由" + trainMap.get("startStation") + "站发往" + trainMap.get("arrivalStation") + "站的" + trainId + "次列车车票，发车时间" + formattedStartTime + "。请合理安排出行时间。";

        // messageService.addMessage(userId, Message.generateMessageId(), oid, "车票订单支付成功", formattedDate, Mcontent, false, "3");
        messageClient.addMessage(new HashMap<>() {{
            put("userId", userId);
            put("orderId", oid);
            put("title", "车票订单支付成功");
            put("messageTime", formattedDate);
            put("content", Mcontent);
            put("orderType", "3");
        }});

        emailService.sendSimpleMail(userClient.getEmail(userId), "火车订单支付成功", content);
        return new HashMap<>() {{
            put("info", "下单成功！");
        }};
    }

    @PostMapping("/ticket/cancel/{userID}/{oid}")
    public Map<String, Object> cancelTrainOrder(@PathVariable String userID,
                                                @PathVariable String oid) {
        OrderDTO order = orderClient.getOrderByOidAndUid(oid, userID);
        if (order == null) {
            return new HashMap<>() {{
                put("info", "订单不存在");
                put("result", false);
            }};
        } else {
            orderClient.cancelOrder(order);

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String formattedDate = formatter.format(date);

            orderClient.setCancelTime(oid, formattedDate);

            List<TrainOrder> trainMap = trainService.getTrainOrdersByOid(oid);

            int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0;
            for (TrainOrder orderDetail : trainMap) {
                String type = orderDetail.getSeatType();
                //trainService.addTrainOrderDetail(oid, trainId, trainDate, person.get("name"), person.get("identification"), type);
                switch (type) {
                    case "商务座":
                        num1 -= 1;
                        break;
                    case "一等座":
                        num2 -= 1;
                        break;
                    case "二等座":
                        num3 -= 1;
                        break;
                    case "软卧":
                        num4 -= 1;
                        break;
                    case "硬卧":
                        num5 -= 1;
                        break;
                    case "硬座":
                        num6 -= 1;
                        break;
                }
            }

            String trainId = trainMap.get(0).getTrainId();
            String trainDate = trainMap.get(0).getTrainDate();
            Map<String, Object> train = trainService.getTrainByIdAndDate(trainId, trainDate);

            // 恢复座位数
            trainService.updateTrainSeat(trainId, trainDate, num1, num2, num3, num4, num5, num6);

            io.github.resilience4j.circuitbreaker.CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("cancelTrainOrder");
//            try {
                // 取消该trainOrder对应的foodOrder
                orderClient.getOrderByUid(userID, "Food").forEach(o -> {
                    // 该user的每个车餐订单
                    foodClient.getFoodOrders(o.getOid()).forEach(fo -> {
                        // System.out.println(o.getOid());
                        if (fo.getTrainDate().equals(trainDate) && fo.getTrainId().equals(trainId)) {
                            // 对应的车次的foodOrder
                            OrderDTO tobeCanceled = orderClient.getOrder(fo.getOid());
                            orderClient.cancelOrder(tobeCanceled);
                        }
                    });
                });
                //            foodClient.getTrainRelatedFoodOrders(trainId, trainDate, userID).forEach(e-> {
                //                orderClient.cancelOrder(e);
                //            });
//            } catch ()


            //String content = "您已成功取消" +trainMap.getTrainDate() + " " + trainMap.getTrainId()+ "车次的列车" + food.getMealTime();
            String content = "【WerwerTrip】您已成功取消" + trainDate + "由" + train.get("startStation") + "站发往" + train.get("arrivalStation") + "站的" + trainId + "次列车车票";
            String Mcontent = "您已成功取消" + trainDate + "由" + train.get("startStation") + "站发往" + train.get("arrivalStation") + "站的" + trainId + "次列车车票";
            // messageService.addMessage(userID, Message.generateMessageId(), oid, "火车订单取消成功", formattedDate, Mcontent, false, "3");
            messageClient.addMessage(new HashMap<>() {{
                put("userId", userID);
                put("orderId", oid);
                put("title", "火车订单取消成功");
                put("messageTime", formattedDate);
                put("content", Mcontent);
                put("orderType", "3");
            }});

            emailService.sendSimpleMail(userClient.getEmail(userID), "火车订单取消成功", content);

            return new HashMap<>() {{
                put("info", "取消成功");
                put("result", true);
            }};
        }
    }

    public Map<String, Object> fallbackCancelTrainOrder(@PathVariable String userID,
                                                @PathVariable String oid) {
        OrderDTO order = orderClient.getOrderByOidAndUid(oid, userID);
        if (order == null) {
            return new HashMap<>() {{
                put("info", "订单不存在");
                put("result", false);
            }};
        } else {
            orderClient.cancelOrder(order);

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String formattedDate = formatter.format(date);

            orderClient.setCancelTime(oid, formattedDate);

            List<TrainOrder> trainMap = trainService.getTrainOrdersByOid(oid);

            int num1 = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0;
            for (TrainOrder orderDetail : trainMap) {
                String type = orderDetail.getSeatType();
                //trainService.addTrainOrderDetail(oid, trainId, trainDate, person.get("name"), person.get("identification"), type);
                switch (type) {
                    case "商务座":
                        num1 -= 1;
                        break;
                    case "一等座":
                        num2 -= 1;
                        break;
                    case "二等座":
                        num3 -= 1;
                        break;
                    case "软卧":
                        num4 -= 1;
                        break;
                    case "硬卧":
                        num5 -= 1;
                        break;
                    case "硬座":
                        num6 -= 1;
                        break;
                }
            }

            String trainId = trainMap.get(0).getTrainId();
            String trainDate = trainMap.get(0).getTrainDate();
            Map<String, Object> train = trainService.getTrainByIdAndDate(trainId, trainDate);

            // 恢复座位数
            trainService.updateTrainSeat(trainId, trainDate, num1, num2, num3, num4, num5, num6);


//            // 取消该trainOrder对应的foodOrder
//            orderClient.getOrderByUid(userID, "Food").forEach(o-> {
//                // 该user的每个车餐订单
//                foodClient.getFoodOrders(o.getOid()).forEach(fo-> {
//                    // System.out.println(o.getOid());
//                    if (fo.getTrainDate().equals(trainDate) && fo.getTrainId().equals(trainId)) {
//                        // 对应的车次的foodOrder
//                        OrderDTO tobeCanceled = orderClient.getOrder(fo.getOid());
//                        orderClient.cancelOrder(tobeCanceled);
//                    }
//                });
//            });
//            foodClient.getTrainRelatedFoodOrders(trainId, trainDate, userID).forEach(e-> {
//                orderClient.cancelOrder(e);
//            });


            //String content = "您已成功取消" +trainMap.getTrainDate() + " " + trainMap.getTrainId()+ "车次的列车" + food.getMealTime();
            String content = "【WerwerTrip】服务繁忙，未能取消相应车餐。您已成功取消" + trainDate + "由" + train.get("startStation") + "站发往" + train.get("arrivalStation") + "站的" + trainId + "次列车车票";
            String Mcontent = "服务繁忙，未能取消相应车餐。您已成功取消" + trainDate + "由" + train.get("startStation") + "站发往" + train.get("arrivalStation") + "站的" + trainId + "次列车车票";
            // messageService.addMessage(userID, Message.generateMessageId(), oid, "火车订单取消成功", formattedDate, Mcontent, false, "3");
            messageClient.addMessage(new HashMap<>() {{
                put("userId", userID);
                put("orderId", oid);
                put("title", "火车订单取消成功");
                put("messageTime", formattedDate);
                put("content", Mcontent);
                put("orderType", "3");
            }});

            emailService.sendSimpleMail(userClient.getEmail(userID), "火车订单取消成功", content);

            return new HashMap<>() {{
                put("info", "取消成功，取消车餐失败");
                put("result", true);
            }};
        }
    }

    @GetMapping("/ticket/orders/{userID}/{status}")
    Map<String, Object> getOrders(@PathVariable String userID,
                                  @PathVariable String status) {
        List<OrderDTO> orders = switch (status) {
            case "paid" -> orderClient.getOrdersByUidAndStatus(userID, "Paid", "Train");
            case "cancel" ->
                    orderClient.getOrdersByUidAndStatus(userID, "Canceled", "Train");
            case "done" -> orderClient.getOrdersByUidAndStatus(userID, "Done", "Train");
            default -> orderClient.getOrderByUid(userID, "Train");
        };

        List<Map<String, Object>> result = new ArrayList<>();
        for (OrderDTO order : orders) {
            HashMap<String, Object> map = new HashMap<>();
            String oid = order.getOid();
            List<TrainOrder> trainOrders = trainService.getTrainOrdersByOid(oid);
            if (trainOrders.isEmpty()) {
                continue;
            }
            String tid = trainOrders.get(0).getTrainId();
            String date = trainOrders.get(0).getTrainDate();
            Train train = trainService.getTrainByTidAndDate(tid, date);

            map.put("tid", tid);
            map.put("cancel_time", order.getCancelTime());
            map.put("oid", order.getOid());
            map.put("start_time", train.getStartTime());
            map.put("start_station", train.getStartStation());
            map.put("arrive_station", train.getArrivalStation());
            map.put("time", train.getDuration());
            map.put("arrive_time", train.getArrivalTime());
            map.put("date", date);
            map.put("order_time", order.getBillTime());
            map.put("status", switch (order.getOrderStatus()) {
                case "Paid" -> "已支付";
                case "Done" -> "已完成";
                case "Canceled" -> "已取消";
                default -> throw new IllegalStateException("Unexpected value: " + order.getOrderStatus());
            });
            map.put("sum_price", order.getTotal());

            List<Object> person = new ArrayList<>();

            trainOrders.forEach(trainOrder -> {
                person.add(new HashMap<>() {{
                    put("name", trainOrder.getName());
                    put("identification", trainOrder.getIdentification());
                    put("seat_type", trainOrder.getSeatType());
                }});
            });

            map.put("person", person);
            result.add(map);
        };

        // 创建一个 Comparator，按照 Map 中的 "orderTime" 字段降序排列
        Comparator<Map<String, Object>> orderTimeComparator = (map1, map2) -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date1 = null;
            try {
                date1 = sdf.parse((String) map1.get("order_time"));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            Date date2 = null;
            try {
                date2 = sdf.parse((String) map2.get("order_time"));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            return date2.compareTo(date1);  // 降序排序
        };

        result.sort(orderTimeComparator);


        return new HashMap<>() {{
            put("result", result);
        }};
    }

    @GetMapping("/getTid/{userID}")
    public Map<String, Object> getSelfOrder(@PathVariable String userID,
                                            @RequestParam(value = "status", defaultValue = "all") String status) {

        List<OrderDTO> orders = switch (status) {
            case "paid" -> orderClient.getOrdersByUidAndStatus(userID, "Paid", "Train");
            case "cancel" ->
                    orderClient.getOrdersByUidAndStatus(userID, "Canceled", "Train");
            case "done" -> orderClient.getOrdersByUidAndStatus(userID, "Done", "Train");
            default -> orderClient.getOrderByUid(userID, "Train");
        };


        List<Object> result = new ArrayList<>();
        for (OrderDTO order : orders) {
            Map<String, Object> orderMap = trainService.getSelfOrderDetail(order.getOid(), userID);
            if (orderMap != null) {
//                System.out.println(order.getOrderStatus());
                result.add(new HashMap<>() {{
                    put("tid", orderMap.get("trainId"));
                    put("oid", order.getOid());
                    put("status", switch (order.getOrderStatus()) {
                        case "Paid" -> "已支付";
                        case "Done" -> "已完成";
                        case "Canceled" -> "已取消";
                        default -> throw new IllegalStateException("Unexpected value: " + order.getOrderStatus());
                    });
                    //put("status", finalOrderStatus);

                    Map<String, Object> trainMap = trainService.getTrainByIdAndDate(orderMap.get("trainId").toString(), orderMap.get("trainDate").toString());
                    LocalDateTime startTime = (LocalDateTime) trainMap.get("startTime");
                    String formattedStartTime = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    LocalDateTime arrivalTime = (LocalDateTime) trainMap.get("arrivalTime");
                    String formattedArrivalTime = arrivalTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                    LocalDateTime billTime = LocalDateTime.parse(order.getBillTime(), formatter);
                    String formattedBillTime = billTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                    put("start_station", trainMap.get("startStation"));
                    put("start_time", formattedStartTime);
                    put("arrive_time", formattedArrivalTime);
                    put("order_time", formattedBillTime);
                    put("time", trainMap.get("duration"));
                    put("arrive_station", trainMap.get("arrivalStation"));
                    put("date", orderMap.get("trainDate"));
                    put("seat_type", orderMap.get("seatType"));
                    put("price", order.getTotal());
                }});
            }

        }
        return new HashMap<>() {{
            put("result", result);
        }};

    }

}
