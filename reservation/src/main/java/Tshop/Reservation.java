package Tshop;

import javax.persistence.*;

import Tshop.external.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.util.List;

@Entity
@Table(name="Reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long reservationId;
    private Long productId;
    private String status;

    /**
     * 예약접수를 신청하면 상품수량을 확인하고 예약가능/불가여부를 판단
     * */
    @PrePersist
    public void onPrePersist(){
        //Tshop.external.Product product = new Tshop.external.Product();

        String checkQuantity = ReservationApplication.applicationContext.getBean(Tshop.external.ProductService.class).checkProductQuantity(this.getProductId().toString());

        if(Integer.parseInt(checkQuantity) > 0){
            this.setStatus("예약신청");
        }else{
            this.setStatus("예약불가");
        }
    }
    /**
     * 예약신청 가능이면 배정관리서비스로 예약번호 전송
     * */
    @PostPersist
    public void onPostPersist(){
        ReservationRequested reservationRequested = new ReservationRequested();
        BeanUtils.copyProperties(this, reservationRequested);
        if("예약신청".equals(this.getStatus())) reservationRequested.publishAfterCommit();
    }
    /**
     * 예약취소 이 후 상품재고 원복 및 배정정보 삭제 이벤트 전달
     * */
    @PostUpdate
    public  void onPostUpdate(){
        if("예약취소".equals(this.getStatus())){
            ReservationCancelRequested reservationCancelRequested = new ReservationCancelRequested();
            BeanUtils.copyProperties(this, reservationCancelRequested);
            reservationCancelRequested.publishAfterCommit();
        }
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }




}
